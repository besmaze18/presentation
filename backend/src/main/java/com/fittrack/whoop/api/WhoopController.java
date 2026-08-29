package com.fittrack.whoop.api;

import com.fittrack.common.security.AuthenticatedUser;
import com.fittrack.common.security.CurrentUser;
import com.fittrack.whoop.config.WhoopProperties;
import com.fittrack.whoop.domain.WhoopConnection;
import com.fittrack.whoop.domain.WhoopCycleRepository;
import com.fittrack.whoop.domain.WhoopRecoveryRepository;
import com.fittrack.whoop.domain.WhoopSleepRepository;
import com.fittrack.whoop.domain.WhoopWorkoutRepository;
import com.fittrack.whoop.dto.WhoopAuthorizationResponse;
import com.fittrack.whoop.dto.WhoopConnectionStatusResponse;
import com.fittrack.whoop.dto.WhoopSyncResult;
import com.fittrack.whoop.service.WhoopOAuthService;
import com.fittrack.whoop.service.WhoopSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WHOOP connection management.
 *
 * <p>The frontend never sees a token: it asks for an authorization URL, sends the browser there,
 * and afterwards reads only a connection status. The callback is the one unauthenticated endpoint,
 * because WHOOP redirects the browser to it directly; it is authorised by the single-use
 * {@code state} parameter issued when the flow began.
 */
@RestController
@RequestMapping("/api/whoop")
@Tag(name = "WHOOP", description = "Connecting a WHOOP account and synchronising its data")
public class WhoopController {

    private static final Logger log = LoggerFactory.getLogger(WhoopController.class);

    private final WhoopOAuthService oAuthService;
    private final WhoopSyncService syncService;
    private final WhoopProperties properties;
    private final WhoopCycleRepository cycleRepository;
    private final WhoopRecoveryRepository recoveryRepository;
    private final WhoopSleepRepository sleepRepository;
    private final WhoopWorkoutRepository workoutRepository;

    public WhoopController(
            WhoopOAuthService oAuthService,
            WhoopSyncService syncService,
            WhoopProperties properties,
            WhoopCycleRepository cycleRepository,
            WhoopRecoveryRepository recoveryRepository,
            WhoopSleepRepository sleepRepository,
            WhoopWorkoutRepository workoutRepository) {
        this.oAuthService = oAuthService;
        this.syncService = syncService;
        this.properties = properties;
        this.cycleRepository = cycleRepository;
        this.recoveryRepository = recoveryRepository;
        this.sleepRepository = sleepRepository;
        this.workoutRepository = workoutRepository;
    }

    @GetMapping("/status")
    @Operation(summary = "Whether WHOOP is configured and connected, plus what has been imported")
    public WhoopConnectionStatusResponse status(@CurrentUser AuthenticatedUser currentUser) {
        if (!properties.isConfigured()) {
            return WhoopConnectionStatusResponse.notConfigured();
        }
        UUID userId = currentUser.getId();
        Optional<WhoopConnection> connection = oAuthService.findConnection(userId);
        return connection
                .map(value -> WhoopConnectionStatusResponse.of(
                        value,
                        cycleRepository.countByUserId(userId),
                        recoveryRepository.countByUserId(userId),
                        sleepRepository.countByUserId(userId),
                        workoutRepository.countByUserId(userId)))
                .orElseGet(WhoopConnectionStatusResponse::notConnected);
    }

    @PostMapping("/authorize")
    @Operation(summary = "Start the OAuth flow and return the URL to send the browser to")
    public WhoopAuthorizationResponse authorize(@CurrentUser AuthenticatedUser currentUser) {
        return new WhoopAuthorizationResponse(oAuthService.beginAuthorization(currentUser.getId()));
    }

    /**
     * WHOOP redirects the browser here. The response is a redirect back into the SPA carrying a
     * simple outcome flag, so the user lands on a page that can explain what happened.
     */
    @GetMapping("/callback")
    @Operation(summary = "OAuth callback, reached by a browser redirect from WHOOP")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error) {

        if (error != null && !error.isBlank()) {
            log.info("WHOOP authorization was declined or failed");
            return redirect("denied");
        }
        try {
            oAuthService.completeAuthorization(code, state);
        } catch (RuntimeException ex) {
            log.warn("WHOOP authorization callback failed: {}", ex.getMessage());
            return redirect("error");
        }
        return redirect("connected");
    }

    @PostMapping("/sync")
    @Operation(
            summary = "Run a synchronisation now",
            description =
                    "The first run reaches back the configured number of days; later runs sync "
                            + "incrementally with a short overlap. Records are upserted on their WHOOP "
                            + "identifier, so repeated runs never duplicate data.")
    public WhoopSyncResult sync(@CurrentUser AuthenticatedUser currentUser) {
        return syncService.sync(currentUser.getId());
    }

    @DeleteMapping("/connection")
    @Operation(summary = "Disconnect WHOOP and revoke the stored grant")
    public ResponseEntity<Void> disconnect(@CurrentUser AuthenticatedUser currentUser) {
        oAuthService.disconnect(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Void> redirect(String outcome) {
        String separator = properties.getAppRedirectUri().contains("?") ? "&" : "?";
        URI target = URI.create(properties.getAppRedirectUri()
                + separator
                + "whoop="
                + URLEncoder.encode(outcome, StandardCharsets.UTF_8));
        return ResponseEntity.status(302).location(target).build();
    }
}
