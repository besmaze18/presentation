package com.fittrack.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.support.ApiTestClient;
import com.fittrack.support.IntegrationTest;
import com.fittrack.support.TestSupportConfiguration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Every /me endpoint must resolve the caller from the token, never from a client-supplied id. */
@IntegrationTest
@Import(TestSupportConfiguration.class)
class UserDataIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiTestClient apiTestClient;

    @Test
    void eachUserSeesOnlyTheirOwnProfileAndGoals() throws Exception {
        ApiTestClient.Session alice = apiTestClient.registerRandomUser();
        ApiTestClient.Session bob = apiTestClient.registerRandomUser();

        mockMvc.perform(put("/api/users/me/goals")
                        .header(HttpHeaders.AUTHORIZATION, alice.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "calorieTarget", 3100,
                                "proteinTargetG", 200,
                                "carbsTargetG", 330,
                                "fatTargetG", 90,
                                "fiberTargetG", 38))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me/goals").header(HttpHeaders.AUTHORIZATION, alice.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].calorieTarget").value(3100));

        // Bob still has his own default goal - Alice's change is invisible to him.
        mockMvc.perform(get("/api/users/me/goals").header(HttpHeaders.AUTHORIZATION, bob.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].calorieTarget").value(2400));

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bob.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(bob.email()));
    }

    @Test
    void settingsUpdatesAreScopedToTheCaller() throws Exception {
        ApiTestClient.Session alice = apiTestClient.registerRandomUser();
        ApiTestClient.Session bob = apiTestClient.registerRandomUser();

        mockMvc.perform(put("/api/users/me/settings")
                        .header(HttpHeaders.AUTHORIZATION, alice.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("timeZone", "Europe/Berlin", "sex", "FEMALE", "heightCm", 172.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeZone").value("Europe/Berlin"));

        mockMvc.perform(get("/api/users/me/settings").header(HttpHeaders.AUTHORIZATION, bob.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeZone").value("UTC"));
    }

    @Test
    void rejectsAnUnknownTimeZone() throws Exception {
        ApiTestClient.Session session = apiTestClient.registerRandomUser();

        mockMvc.perform(put("/api/users/me/settings")
                        .header(HttpHeaders.AUTHORIZATION, session.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("timeZone", "Mars/Olympus_Mons"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
