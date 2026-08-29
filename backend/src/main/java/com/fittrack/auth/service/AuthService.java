package com.fittrack.auth.service;

import com.fittrack.auth.domain.RefreshToken;
import com.fittrack.auth.domain.RefreshTokenRepository;
import com.fittrack.auth.dto.AuthTokens;
import com.fittrack.auth.dto.LoginRequest;
import com.fittrack.auth.dto.RegisterRequest;
import com.fittrack.common.exception.ConflictException;
import com.fittrack.common.exception.ForbiddenException;
import com.fittrack.common.exception.UnauthorizedException;
import com.fittrack.common.security.JwtProperties;
import com.fittrack.common.security.JwtService;
import com.fittrack.common.security.SecurityProperties;
import com.fittrack.user.domain.User;
import com.fittrack.user.domain.UserRepository;
import com.fittrack.user.service.UserProvisioningService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registration, login, refresh-token rotation and logout. */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int REFRESH_TOKEN_BYTES = 48;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserProvisioningService userProvisioningService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final SecurityProperties securityProperties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserProvisioningService userProvisioningService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties,
            SecurityProperties securityProperties,
            Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userProvisioningService = userProvisioningService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.securityProperties = securityProperties;
        this.clock = clock;
    }

    @Transactional
    public RegistrationResult register(RegisterRequest request) {
        if (!securityProperties.isRegistrationEnabled()) {
            throw new ForbiddenException("Registration is disabled on this instance");
        }
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with that email already exists");
        }
        User user = new User(email, passwordEncoder.encode(request.password()), request.displayName().trim());
        user = userRepository.save(user);
        userProvisioningService.initialiseNewUser(user, request.timeZone());
        log.info("Registered user {}", user.getId());
        return new RegistrationResult(user, issueTokens(user, UUID.randomUUID()));
    }

    @Transactional
    public RegistrationResult login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);
        // Always run a hash comparison so a missing account and a wrong password take similar time.
        String storedHash = user != null ? user.getPasswordHash() : "$2a$12$invalidinvalidinvalidinvalidinvalidinvalidinvalidinvalidinv";
        boolean matches = passwordEncoder.matches(request.password(), storedHash);
        if (user == null || !matches) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new ForbiddenException("This account is disabled");
        }
        return new RegistrationResult(user, issueTokens(user, UUID.randomUUID()));
    }

    /**
     * Rotates a refresh token. Presenting a token that was already rotated (or revoked) is treated
     * as theft and revokes the entire rotation family.
     */
    @Transactional
    public RegistrationResult refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is missing");
        }
        Instant now = clock.instant();
        RefreshToken stored = refreshTokenRepository
                .findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException("Refresh token is not valid"));

        if (!stored.isActive(now)) {
            refreshTokenRepository.revokeFamily(stored.getFamilyId(), now);
            log.warn("Refresh token reuse detected for user {}; revoked token family", stored.getUser().getId());
            throw new UnauthorizedException("Refresh token is not valid");
        }

        User user = stored.getUser();
        if (!user.isEnabled()) {
            throw new ForbiddenException("This account is disabled");
        }

        AuthTokens tokens = issueTokens(user, stored.getFamilyId());
        RefreshToken replacement = refreshTokenRepository
                .findByTokenHash(hash(tokens.refreshToken()))
                .orElseThrow(() -> new IllegalStateException("Replacement refresh token was not persisted"));
        stored.setRevokedAt(now);
        stored.setReplacedById(replacement.getId());
        refreshTokenRepository.save(stored);
        return new RegistrationResult(user, tokens);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository
                .findByTokenHash(hash(rawRefreshToken))
                .ifPresent(token -> refreshTokenRepository.revokeFamily(token.getFamilyId(), clock.instant()));
    }

    @Transactional
    public void logoutAllSessions(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, clock.instant());
    }

    private AuthTokens issueTokens(User user, UUID familyId) {
        Instant now = clock.instant();
        String accessToken =
                jwtService.issueAccessToken(user.getId(), user.getEmail(), user.getRole().name(), now);

        byte[] raw = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(raw);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        refreshTokenRepository.saveAndFlush(new RefreshToken(
                user, hash(refreshToken), familyId, now.plus(jwtProperties.getRefreshTokenTtl())));

        return new AuthTokens(accessToken, jwtService.accessTokenTtlSeconds(), refreshToken);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public record RegistrationResult(User user, AuthTokens tokens) {}
}
