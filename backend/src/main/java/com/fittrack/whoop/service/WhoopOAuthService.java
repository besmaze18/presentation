package com.fittrack.whoop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fittrack.common.exception.BadRequestException;
import com.fittrack.common.exception.ConflictException;
import com.fittrack.common.exception.NotFoundException;
import com.fittrack.common.util.CryptoService;
import com.fittrack.user.domain.User;
import com.fittrack.user.service.UserService;
import com.fittrack.whoop.client.WhoopApiException;
import com.fittrack.whoop.client.WhoopClient;
import com.fittrack.whoop.client.WhoopTokens;
import com.fittrack.whoop.config.WhoopProperties;
import com.fittrack.whoop.domain.WhoopConnection;
import com.fittrack.whoop.domain.WhoopConnectionRepository;
import com.fittrack.whoop.domain.WhoopConnectionStatus;
import com.fittrack.whoop.domain.WhoopOAuthState;
import com.fittrack.whoop.domain.WhoopOAuthStateRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The WHOOP OAuth 2.0 authorization-code flow and token lifecycle.
 *
 * <p>Client secrets and tokens never leave the server: the frontend receives an authorization URL
 * to redirect to, and afterwards only a connection status. Tokens are encrypted at rest and
 * refreshed transparently just before expiry.
 */
@Service
public class WhoopOAuthService {

    private static final Logger log = LoggerFactory.getLogger(WhoopOAuthService.class);
    private static final int STATE_BYTES = 32;

    private final WhoopProperties properties;
    private final WhoopClient whoopClient;
    private final WhoopConnectionRepository connectionRepository;
    private final WhoopOAuthStateRepository stateRepository;
    private final CryptoService cryptoService;
    private final WhoopConnectionStateRecorder stateRecorder;
    private final UserService userService;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public WhoopOAuthService(
            WhoopProperties properties,
            WhoopClient whoopClient,
            WhoopConnectionRepository connectionRepository,
            WhoopOAuthStateRepository stateRepository,
            CryptoService cryptoService,
            WhoopConnectionStateRecorder stateRecorder,
            UserService userService,
            Clock clock) {
        this.properties = properties;
        this.whoopClient = whoopClient;
        this.connectionRepository = connectionRepository;
        this.stateRepository = stateRepository;
        this.cryptoService = cryptoService;
        this.stateRecorder = stateRecorder;
        this.userService = userService;
        this.clock = clock;
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    /** Issues a single-use state value and the URL the user should be sent to. */
    @Transactional
    public String beginAuthorization(UUID userId) {
        requireConfigured();
        byte[] raw = new byte[STATE_BYTES];
        secureRandom.nextBytes(raw);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        stateRepository.save(
                new WhoopOAuthState(state, userId, clock.instant().plus(properties.getStateTtl())));
        return whoopClient.buildAuthorizationUrl(state);
    }

    /**
     * Completes the flow. The state parameter is validated and consumed before the code is
     * exchanged, so a forged or replayed callback is rejected without ever contacting WHOOP.
     */
    @Transactional
    public WhoopConnection completeAuthorization(String code, String state) {
        requireConfigured();
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Authorization code is missing");
        }

        Instant now = clock.instant();
        WhoopOAuthState storedState = stateRepository
                .findByState(state == null ? "" : state)
                .orElseThrow(() -> new BadRequestException("Invalid OAuth state"));
        if (!storedState.isUsable(now)) {
            throw new BadRequestException("This authorization link has expired or was already used");
        }
        storedState.consume(now);
        stateRepository.save(storedState);

        User user = userService.requireUser(storedState.getUserId());
        WhoopTokens tokens = whoopClient.exchangeAuthorizationCode(code);

        JsonNode profile = whoopClient.getProfile(tokens.accessToken());
        Long whoopUserId = profile.path("user_id").isNumber() ? profile.path("user_id").asLong() : null;
        if (whoopUserId == null) {
            throw new WhoopApiException(502, "WHOOP did not return a user id for this authorization");
        }

        // A WHOOP account belongs to exactly one application account.
        Optional<WhoopConnection> existingForWhoopUser = connectionRepository.findByWhoopUserId(whoopUserId);
        if (existingForWhoopUser.isPresent()
                && !existingForWhoopUser.get().getUser().getId().equals(user.getId())) {
            throw new ConflictException("That WHOOP account is already connected to another user");
        }

        WhoopConnection connection = connectionRepository
                .findByUserId(user.getId())
                .orElseGet(() -> new WhoopConnection(user, whoopUserId, now));
        connection.setWhoopUserId(whoopUserId);
        connection.setWhoopEmail(textOrNull(profile, "email"));
        connection.setWhoopFirstName(textOrNull(profile, "first_name"));
        connection.setWhoopLastName(textOrNull(profile, "last_name"));
        connection.setStatus(WhoopConnectionStatus.CONNECTED);
        connection.setConnectedAt(now);
        applyTokens(connection, tokens);

        log.info("Connected WHOOP account for user {}", user.getId());
        return connectionRepository.save(connection);
    }

    /**
     * Returns a usable access token, refreshing first when it is close to expiry. Callers never
     * handle refresh themselves.
     */
    @Transactional
    public String accessTokenFor(WhoopConnection connection) {
        if (!connection.isUsable()) {
            throw new BadRequestException("The WHOOP connection needs to be re-authorised");
        }
        if (!connection.needsRefresh(clock.instant(), properties.getTokenRefreshSkew())) {
            return cryptoService.decrypt(connection.getAccessTokenEncrypted());
        }

        String refreshToken = cryptoService.decrypt(connection.getRefreshTokenEncrypted());
        if (refreshToken == null || refreshToken.isBlank()) {
            // Committed separately - the throw below would otherwise roll this back.
            stateRecorder.markReauthorisationRequired(
                    connection.getId(), "No refresh token is stored for this connection");
            throw new BadRequestException("The WHOOP connection needs to be re-authorised");
        }

        try {
            WhoopTokens refreshed = whoopClient.refreshTokens(refreshToken);
            applyTokens(connection, refreshed);
            connectionRepository.save(connection);
            log.debug("Refreshed WHOOP access token for user {}", connection.getUser().getId());
            return refreshed.accessToken();
        } catch (WhoopApiException ex) {
            if (ex.isAuthorisationFailure()) {
                // WHOOP rejected the grant itself; only the user can fix this by reconnecting.
                // Recorded in its own transaction so the rethrow cannot undo it.
                stateRecorder.markReauthorisationRequired(
                        connection.getId(), "WHOOP rejected the stored refresh token");
            }
            throw ex;
        }
    }

    @Transactional
    public void disconnect(UUID userId) {
        WhoopConnection connection = connectionRepository
                .findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("No WHOOP connection to disconnect"));

        if (connection.isUsable()) {
            try {
                whoopClient.revokeAccess(cryptoService.decrypt(connection.getAccessTokenEncrypted()));
            } catch (RuntimeException ex) {
                // Revocation is best-effort: the local grant is removed either way.
                log.warn("Could not revoke WHOOP access for user {}", userId);
            }
        }
        connectionRepository.delete(connection);
        log.info("Disconnected WHOOP for user {}", userId);
    }

    @Transactional(readOnly = true)
    public Optional<WhoopConnection> findConnection(UUID userId) {
        return connectionRepository.findByUserId(userId);
    }

    private void applyTokens(WhoopConnection connection, WhoopTokens tokens) {
        connection.setAccessTokenEncrypted(cryptoService.encrypt(tokens.accessToken()));
        if (tokens.refreshToken() != null && !tokens.refreshToken().isBlank()) {
            connection.setRefreshTokenEncrypted(cryptoService.encrypt(tokens.refreshToken()));
        }
        connection.setAccessTokenExpiresAt(tokens.expiresAt());
        if (tokens.scope() != null) {
            connection.setScopes(tokens.scope());
        }
    }

    private void requireConfigured() {
        if (!properties.isConfigured()) {
            throw new BadRequestException(
                    "WHOOP is not configured on this instance. Set WHOOP_CLIENT_ID and WHOOP_CLIENT_SECRET.");
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
    }
}
