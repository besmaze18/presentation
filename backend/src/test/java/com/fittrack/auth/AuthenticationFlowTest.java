package com.fittrack.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.support.ApiTestClient;
import com.fittrack.support.CommittedIntegrationTest;
import com.fittrack.support.DatabaseCleaner;
import com.fittrack.support.TestSupportConfiguration;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@CommittedIntegrationTest
@Import(TestSupportConfiguration.class)
class AuthenticationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiTestClient apiTestClient;

    @Test
    void registersAUserAndReturnsAnAccessTokenPlusAnHttpOnlyRefreshCookie() throws Exception {
        String email = "register-" + UUID.randomUUID() + "@example.test";

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "correct-horse-battery-staple",
                                "displayName", "Alex",
                                "timeZone", "Europe/Berlin"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.displayName").value("Alex"))
                // The refresh token has no field on the response at all - it travels only in the
                // HttpOnly cookie, so this must hold regardless of null-serialisation settings.
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("fittrack_refresh");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/api/auth");
        assertThat(cookie.getValue()).isNotBlank();
    }

    @Test
    void rejectsWeakOrMalformedRegistrations() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "not-an-email",
                                "password", "short",
                                "displayName", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void refusesDuplicateEmailAddressesCaseInsensitively() throws Exception {
        String email = "dupe-" + UUID.randomUUID() + "@example.test";
        apiTestClient.register(email, "correct-horse-battery-staple", "First");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email.toUpperCase(),
                                "password", "correct-horse-battery-staple",
                                "displayName", "Second"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void logsInWithValidCredentialsAndRejectsInvalidOnes() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", session.email(), "password", session.password()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", session.email(), "password", "wrong-password-entirely"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "nobody-" + UUID.randomUUID() + "@example.test",
                                "password", "correct-horse-battery-staple"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireABearerToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());

        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(session.userId().toString()));
    }

    @Test
    void rotatesTheRefreshTokenAndRevokesTheFamilyWhenAnOldTokenIsReplayed() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();
        String firstRefresh = session.refreshToken();

        MvcResult rotated = mockMvc.perform(
                        post("/api/auth/refresh").cookie(new Cookie("fittrack_refresh", firstRefresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        String secondRefresh = rotated.getResponse().getCookie("fittrack_refresh").getValue();
        assertThat(secondRefresh).isNotEqualTo(firstRefresh);

        // Replaying the consumed token is treated as theft: it fails and burns the whole family.
        mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("fittrack_refresh", firstRefresh)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("fittrack_refresh", secondRefresh)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshWithoutACookieIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesTheRefreshTokenAndClearsTheCookie() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        MvcResult logout = mockMvc.perform(post("/api/auth/logout")
                        .cookie(new Cookie("fittrack_refresh", session.refreshToken())))
                .andExpect(status().isNoContent())
                .andReturn();

        assertThat(logout.getResponse().getCookie("fittrack_refresh").getMaxAge()).isZero();

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("fittrack_refresh", session.refreshToken())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationProvisionsSettingsAndAStartingNutritionGoal() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(get("/api/users/me/settings").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeZone").value("UTC"))
                .andExpect(jsonPath("$.unitSystem").value("METRIC"));

        MvcResult goals = mockMvc.perform(
                        get("/api/users/me/goals").header(HttpHeaders.AUTHORIZATION, session.bearer()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(goals.getResponse().getContentAsString());
        assertThat(body).hasSize(1);
        assertThat(body.get(0).path("calorieTarget").asInt()).isPositive();
    }

    @org.junit.jupiter.api.AfterEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }

}
