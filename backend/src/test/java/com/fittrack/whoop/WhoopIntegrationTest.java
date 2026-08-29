package com.fittrack.whoop;

import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.common.util.CryptoService;
import com.fittrack.support.ApiTestClient;
import com.fittrack.support.IntegrationTest;
import com.fittrack.support.TestSupportConfiguration;
import com.fittrack.training.domain.TrainingSession;
import com.fittrack.training.domain.TrainingSessionRepository;
import com.fittrack.training.domain.TrainingSource;
import com.fittrack.whoop.domain.WhoopConnection;
import com.fittrack.whoop.domain.WhoopConnectionRepository;
import com.fittrack.whoop.domain.WhoopConnectionStatus;
import com.fittrack.whoop.domain.WhoopCycle;
import com.fittrack.whoop.domain.WhoopCycleRepository;
import com.fittrack.whoop.domain.WhoopRecoveryRepository;
import com.fittrack.whoop.domain.WhoopSleep;
import com.fittrack.whoop.domain.WhoopSleepRepository;
import com.fittrack.whoop.domain.WhoopWorkoutRepository;
import com.fittrack.whoop.service.WhoopSyncService;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * The WHOOP integration end to end against a stubbed API: OAuth state validation, token storage
 * and refresh, idempotent synchronisation, and the projection of workouts into training sessions.
 */
@IntegrationTest
@Import(TestSupportConfiguration.class)
class WhoopIntegrationTest {

    private static WhoopApiStub whoop;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiTestClient apiTestClient;

    @Autowired
    private WhoopConnectionRepository connectionRepository;

    @Autowired
    private WhoopCycleRepository cycleRepository;

    @Autowired
    private WhoopRecoveryRepository recoveryRepository;

    @Autowired
    private WhoopSleepRepository sleepRepository;

    @Autowired
    private WhoopWorkoutRepository workoutRepository;

    @Autowired
    private TrainingSessionRepository trainingSessionRepository;

    @Autowired
    private WhoopSyncService syncService;

    @Autowired
    private CryptoService cryptoService;

    @BeforeAll
    static void startStub() {
        whoop = WhoopApiStub.start();
    }

    @AfterAll
    static void stopStub() {
        whoop.stop();
    }

    @DynamicPropertySource
    static void whoopProperties(DynamicPropertyRegistry registry) {
        // The stub is started before the context, so its port can be bound here.
        WhoopApiStub stub = whoop != null ? whoop : (whoop = WhoopApiStub.start());
        String base = "http://localhost:" + stub.port();
        registry.add("fittrack.whoop.enabled", () -> true);
        registry.add("fittrack.whoop.client-id", () -> "test-client-id");
        registry.add("fittrack.whoop.client-secret", () -> "test-client-secret");
        registry.add("fittrack.whoop.api-base-url", () -> base + "/developer");
        registry.add("fittrack.whoop.authorize-url", () -> base + "/oauth/oauth2/auth");
        registry.add("fittrack.whoop.token-url", () -> base + "/oauth/oauth2/token");
        registry.add("fittrack.whoop.revoke-url", () -> base + "/developer/v2/user/access");
        registry.add("fittrack.whoop.redirect-uri", () -> "http://localhost:8080/api/whoop/callback");
        registry.add("fittrack.whoop.app-redirect-uri", () -> "http://localhost:5173/settings");
    }

    @BeforeEach
    void resetStub() {
        whoop.reset();
        whoop.stubProfile();
        whoop.stubTokenExchange("access-token-1", "refresh-token-1", 3600);
        whoop.stubCollections();
        whoop.stubBodyMeasurement(82.5);
        whoop.server()
                .stubFor(com.github.tomakehurst.wiremock.client.WireMock.delete(
                                urlPathEqualTo("/developer/v2/user/access"))
                        .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(204)));
    }

    // ------------------------------------------------------------------ OAuth

    private String beginAuthorization(ApiTestClient.Session session) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/whoop/authorize").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andReturn();
        String url = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .path("authorizationUrl")
                .asText();
        return extractQueryParam(url, "state");
    }

    private void completeCallback(String state, String expectedOutcome) throws Exception {
        mockMvc.perform(get("/api/whoop/callback").param("code", "auth-code-1").param("state", state))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION, "http://localhost:5173/settings?whoop=" + expectedOutcome));
    }

    private ApiTestClient.Session connectedUser() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        completeCallback(beginAuthorization(session), "connected");
        return session;
    }

    @Test
    void theAuthorizationUrlCarriesTheConfiguredClientIdRedirectAndScopes() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        MvcResult result = mockMvc.perform(
                        post("/api/whoop/authorize").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andReturn();

        String url = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .path("authorizationUrl")
                .asText();

        assertThat(url).contains("/oauth/oauth2/auth");
        assertThat(extractQueryParam(url, "client_id")).isEqualTo("test-client-id");
        assertThat(extractQueryParam(url, "response_type")).isEqualTo("code");
        assertThat(extractQueryParam(url, "redirect_uri"))
                .isEqualTo("http://localhost:8080/api/whoop/callback");
        assertThat(extractQueryParam(url, "scope")).contains("read:recovery").contains("offline");
        assertThat(extractQueryParam(url, "state")).isNotBlank();
        // The client secret must never appear in anything the browser sees.
        assertThat(url).doesNotContain("test-client-secret");
    }

    @Test
    void theCallbackRejectsAMissingForgedOrReplayedState() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(get("/api/whoop/callback").param("code", "auth-code-1"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost:5173/settings?whoop=error"));

        mockMvc.perform(get("/api/whoop/callback")
                        .param("code", "auth-code-1")
                        .param("state", "not-a-state-we-issued"))
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost:5173/settings?whoop=error"));

        // A state is single use: the same callback URL cannot be replayed.
        String state = beginAuthorization(session);
        completeCallback(state, "connected");
        completeCallback(state, "error");
    }

    @Test
    void aDeclinedAuthorizationIsReportedRatherThanTreatedAsAnError() throws Exception {
        mockMvc.perform(get("/api/whoop/callback").param("error", "access_denied"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION, "http://localhost:5173/settings?whoop=denied"));
    }

    @Test
    void tokensAreStoredEncryptedAndNeverReturnedByTheApi() throws Exception {
        ApiTestClient.Session session = connectedUser();

        WhoopConnection connection = connectionRepository.findByUserId(session.userId()).orElseThrow();
        assertThat(connection.getAccessTokenEncrypted()).doesNotContain("access-token-1");
        assertThat(connection.getRefreshTokenEncrypted()).doesNotContain("refresh-token-1");
        assertThat(cryptoService.decrypt(connection.getAccessTokenEncrypted())).isEqualTo("access-token-1");
        assertThat(cryptoService.decrypt(connection.getRefreshTokenEncrypted())).isEqualTo("refresh-token-1");
        assertThat(connection.getWhoopUserId()).isEqualTo(WhoopApiStub.WHOOP_USER_ID);

        MvcResult status = mockMvc.perform(
                        get("/api/whoop/status").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(true))
                .andExpect(jsonPath("$.whoopFirstName").value("Alex"))
                .andReturn();

        String body = status.getResponse().getContentAsString();
        assertThat(body)
                .doesNotContain("access-token-1")
                .doesNotContain("refresh-token-1")
                .doesNotContain("test-client-secret");
    }

    @Test
    void anExpiredAccessTokenIsRefreshedBeforeTheNextRequest() throws Exception {
        ApiTestClient.Session session = connectedUser();

        // Force the stored token to look expired.
        WhoopConnection connection = connectionRepository.findByUserId(session.userId()).orElseThrow();
        connection.setAccessTokenExpiresAt(Instant.now().minusSeconds(60));
        connectionRepository.saveAndFlush(connection);

        whoop.stubTokenExchange("access-token-2", "refresh-token-2", 3600);

        mockMvc.perform(post("/api/whoop/sync").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk());

        WhoopConnection refreshed = connectionRepository.findByUserId(session.userId()).orElseThrow();
        assertThat(cryptoService.decrypt(refreshed.getAccessTokenEncrypted())).isEqualTo("access-token-2");
        assertThat(cryptoService.decrypt(refreshed.getRefreshTokenEncrypted())).isEqualTo("refresh-token-2");
        assertThat(refreshed.getAccessTokenExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void aRejectedRefreshTokenMarksTheConnectionAsNeedingReauthorisation() throws Exception {
        ApiTestClient.Session session = connectedUser();

        WhoopConnection connection = connectionRepository.findByUserId(session.userId()).orElseThrow();
        connection.setAccessTokenExpiresAt(Instant.now().minusSeconds(60));
        connectionRepository.saveAndFlush(connection);

        whoop.stubTokenExchangeFailure(401);

        mockMvc.perform(post("/api/whoop/sync").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isBadGateway());

        WhoopConnection after = connectionRepository.findByUserId(session.userId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(WhoopConnectionStatus.REAUTHORISATION_REQUIRED);

        mockMvc.perform(get("/api/whoop/status").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.status").value("REAUTHORISATION_REQUIRED"));
    }

    // ------------------------------------------------------------------- Sync

    @Test
    void theInitialSyncImportsEveryCollection() throws Exception {
        ApiTestClient.Session session = connectedUser();

        mockMvc.perform(post("/api/whoop/sync").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialSync").value(true))
                .andExpect(jsonPath("$.cyclesImported").value(1))
                .andExpect(jsonPath("$.recoveriesImported").value(1))
                .andExpect(jsonPath("$.sleepsImported").value(1))
                .andExpect(jsonPath("$.workoutsImported").value(1))
                .andExpect(jsonPath("$.trainingSessionsCreated").value(1))
                .andExpect(jsonPath("$.bodyMeasurementsImported").value(1));

        UUID userId = session.userId();
        assertThat(cycleRepository.countByUserId(userId)).isEqualTo(1);
        assertThat(recoveryRepository.countByUserId(userId)).isEqualTo(1);
        assertThat(sleepRepository.countByUserId(userId)).isEqualTo(1);
        assertThat(workoutRepository.countByUserId(userId)).isEqualTo(1);
    }

    @Test
    void syncingTwiceUpdatesInsteadOfDuplicating() throws Exception {
        ApiTestClient.Session session = connectedUser();
        UUID userId = session.userId();

        syncService.sync(userId);
        // A later run sees the same records with a changed strain value.
        whoop.reset();
        whoop.stubProfile();
        whoop.stubTokenExchange("access-token-1", "refresh-token-1", 3600);
        whoop.stubBodyMeasurement(82.5);
        whoop.stubCollections(16.9, 71, 7 * 3_600_000L + 20 * 60_000L);

        var second = syncService.sync(userId);

        assertThat(second.initialSync()).isFalse();
        assertThat(second.totalImported()).isZero();
        assertThat(second.cyclesUpdated()).isEqualTo(1);
        assertThat(second.recoveriesUpdated()).isEqualTo(1);
        assertThat(second.workoutsUpdated()).isEqualTo(1);

        assertThat(cycleRepository.countByUserId(userId)).isEqualTo(1);
        assertThat(workoutRepository.countByUserId(userId)).isEqualTo(1);
        assertThat(trainingSessionRepository.findAll()).hasSize(1);

        WhoopCycle cycle = cycleRepository
                .findByUserIdAndWhoopCycleId(userId, WhoopApiStub.CYCLE_ID)
                .orElseThrow();
        assertThat(cycle.getStrain()).isEqualByComparingTo("16.90");
    }

    @Test
    void aWorkoutBecomesATrainingSessionThatKeepsTheUsersOwnEdits() throws Exception {
        ApiTestClient.Session session = connectedUser();
        UUID userId = session.userId();
        syncService.sync(userId);

        TrainingSession imported = trainingSessionRepository
                .findByUserIdAndSourceAndExternalId(userId, TrainingSource.WHOOP, WhoopApiStub.WORKOUT_ID)
                .orElseThrow();
        assertThat(imported.getSportLabel()).isEqualTo("Weightlifting");
        assertThat(imported.getCategory().name()).isEqualTo("STRENGTH");
        assertThat(imported.getDurationMinutes()).isEqualTo(65);
        assertThat(imported.getStrain()).isEqualByComparingTo("12.40");
        assertThat(imported.getAverageHeartRate()).isEqualTo(123);
        // 2175 kJ -> 520 kcal
        assertThat(imported.getCaloriesKcal()).isEqualByComparingTo("520");

        // The user annotates the imported session...
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/training/sessions/" + imported.getId())
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "Heavy squat day",
                                "perceivedExertion", 8,
                                "notes", "Felt strong"))))
                .andExpect(status().isOk());

        // ...and a later sync must not overwrite it.
        syncService.sync(userId);

        TrainingSession afterResync = trainingSessionRepository.findById(imported.getId()).orElseThrow();
        assertThat(afterResync.getTitle()).isEqualTo("Heavy squat day");
        assertThat(afterResync.getPerceivedExertion()).isEqualTo(8);
        assertThat(afterResync.getNotes()).isEqualTo("Felt strong");
        assertThat(afterResync.getStrain()).isEqualByComparingTo("12.40");
    }

    @Test
    void sleepDurationAndNeedAreDerivedFromTheStageSummary() throws Exception {
        ApiTestClient.Session session = connectedUser();
        syncService.sync(session.userId());

        WhoopSleep sleep = sleepRepository
                .findByUserIdAndWhoopSleepId(session.userId(), WhoopApiStub.SLEEP_ID)
                .orElseThrow();

        // 7h20m in bed minus 20 minutes awake = 7h00m actually asleep.
        assertThat(sleep.getSleepDurationMillis()).isEqualTo(7L * 3_600_000L);
        // baseline 7h30 + 30m debt + 15m strain = 8h15m
        assertThat(sleep.getSleepNeedMillis()).isEqualTo(29_700_000L);
        assertThat(sleep.isNap()).isFalse();
        assertThat(sleep.getSleepPerformancePercentage()).isEqualTo(92);
        // Attributed to the day it ended on.
        assertThat(sleep.getSleepDate().toString()).isEqualTo("2026-03-10");
    }

    @Test
    void followsNextTokenPaginationAcrossPages() throws Exception {
        ApiTestClient.Session session = connectedUser();
        whoop.reset();
        whoop.stubTokenExchange("access-token-1", "refresh-token-1", 3600);
        whoop.stubProfile();
        whoop.stubBodyMeasurement(82.5);
        whoop.stubPaginatedCycles();

        var result = syncService.sync(session.userId());

        assertThat(result.cyclesImported()).isEqualTo(2);
        assertThat(cycleRepository.countByUserId(session.userId())).isEqualTo(2);
        whoop.server().verify(exactly(2), getRequestedFor(urlPathEqualTo("/developer/v2/cycle")));
    }

    @Test
    void aFailedSyncIsRecordedOnTheConnectionAndSurfacedToTheUser() throws Exception {
        ApiTestClient.Session session = connectedUser();
        whoop.stubCollectionFailure("/developer/v2/cycle", 500);

        mockMvc.perform(post("/api/whoop/sync").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("EXTERNAL_SERVICE_ERROR"));

        WhoopConnection connection = connectionRepository.findByUserId(session.userId()).orElseThrow();
        assertThat(connection.getLastSyncError()).isNotBlank();
        // A transient outage does not invalidate the grant.
        assertThat(connection.getStatus()).isEqualTo(WhoopConnectionStatus.CONNECTED);
    }

    @Test
    void syncedDataFlowsIntoTheDashboardAndAnalytics() throws Exception {
        ApiTestClient.Session session = connectedUser();
        syncService.sync(session.userId());

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .param("date", "2026-03-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wearableConnected").value(true))
                .andExpect(jsonPath("$.wearable.recoveryScore").value(62))
                .andExpect(jsonPath("$.wearable.strain").value(14.2))
                .andExpect(jsonPath("$.wearable.sleepDurationMillis").value(25200000))
                // 11757 kJ -> 2810 kcal, reported separately from the estimated TDEE.
                .andExpect(jsonPath("$.energy.wearableExpenditureKcal").value(2810))
                .andExpect(jsonPath("$.energy.balanceBasis").value("WEARABLE_EXPENDITURE"))
                .andExpect(jsonPath("$.workoutsToday.length()").value(1))
                .andExpect(jsonPath("$.workoutsToday[0].sportLabel").value("Weightlifting"));
    }

    @Test
    void oneUsersWhoopDataIsInvisibleToAnother() throws Exception {
        ApiTestClient.Session owner = connectedUser();
        syncService.sync(owner.userId());
        ApiTestClient.Session other = apiTestClient.registerRandomUser();

        mockMvc.perform(get("/api/whoop/status").header(HttpHeaders.AUTHORIZATION, other.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.cycleCount").value(0));

        mockMvc.perform(get("/api/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, other.bearer())
                        .param("date", "2026-03-10"))
                .andExpect(jsonPath("$.wearableConnected").value(false))
                .andExpect(jsonPath("$.wearable.recoveryScore").doesNotExist());

        mockMvc.perform(post("/api/whoop/sync").header(HttpHeaders.AUTHORIZATION, other.bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    void theSameWhoopAccountCannotBeConnectedTwice() throws Exception {
        connectedUser();
        ApiTestClient.Session second = apiTestClient.registerRandomUser();

        // The stub always reports the same WHOOP user id.
        completeCallback(beginAuthorization(second), "error");
        assertThat(connectionRepository.findByUserId(second.userId())).isEmpty();
    }

    @Test
    void disconnectingRevokesTheGrantAndRemovesTheConnection() throws Exception {
        ApiTestClient.Session session = connectedUser();

        mockMvc.perform(delete("/api/whoop/connection").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isNoContent());

        assertThat(connectionRepository.findByUserId(session.userId())).isEmpty();
        whoop.server().verify(deleteRequestedFor(urlPathEqualTo("/developer/v2/user/access")));

        mockMvc.perform(get("/api/whoop/status").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(jsonPath("$.connected").value(false))
                .andExpect(jsonPath("$.status").value("NOT_CONNECTED"));
    }

    @Test
    void whoopEndpointsRequireAuthenticationExceptTheCallback() throws Exception {
        mockMvc.perform(get("/api/whoop/status")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/whoop/authorize")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/whoop/sync")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/whoop/connection")).andExpect(status().isUnauthorized());
        // The callback is reached by a browser redirect and is authorised by the state parameter.
        mockMvc.perform(get("/api/whoop/callback").param("error", "access_denied"))
                .andExpect(status().isFound());
    }

    @Test
    void theTokenRequestSendsCredentialsInTheBodyAsWhoopExpects() throws Exception {
        connectedUser();

        List<com.github.tomakehurst.wiremock.verification.LoggedRequest> requests =
                whoop.server().findAll(postRequestedFor(urlPathEqualTo("/oauth/oauth2/token")));
        assertThat(requests).isNotEmpty();

        String body = requests.get(0).getBodyAsString();
        assertThat(body)
                .contains("grant_type=authorization_code")
                .contains("client_id=test-client-id")
                .contains("client_secret=test-client-secret")
                .contains("code=auth-code-1");
        // Credentials go in the body, not a Basic auth header.
        assertThat(requests.get(0).getHeader("Authorization")).isNull();
    }

    private static String extractQueryParam(String url, String name) {
        String query = URI.create(url).getQuery();
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts[0].equals(name)) {
                return java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
