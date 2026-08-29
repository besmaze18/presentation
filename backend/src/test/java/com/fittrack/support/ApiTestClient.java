package com.fittrack.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Registers throwaway users and hands back their bearer tokens. */
@Component
public class ApiTestClient {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    public ApiTestClient(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public Session registerRandomUser() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.test";
        return register(email, "correct-horse-battery-staple", "Test User");
    }

    public Session register(String email, String password, String displayName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password,
                                "displayName", displayName,
                                "timeZone", "UTC"))))
                .andReturn();

        if (result.getResponse().getStatus() != 201) {
            throw new IllegalStateException(
                    "Registration failed: " + result.getResponse().getStatus() + " "
                            + result.getResponse().getContentAsString());
        }

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String refreshCookie = result.getResponse().getCookie("fittrack_refresh") == null
                ? null
                : result.getResponse().getCookie("fittrack_refresh").getValue();
        return new Session(
                UUID.fromString(body.path("user").path("id").asText()),
                email,
                password,
                body.path("accessToken").asText(),
                refreshCookie);
    }

    /** A registered user plus the credentials needed to call the API as them. */
    public record Session(UUID userId, String email, String password, String accessToken, String refreshToken) {

        public String bearer() {
            return "Bearer " + accessToken;
        }
    }
}
