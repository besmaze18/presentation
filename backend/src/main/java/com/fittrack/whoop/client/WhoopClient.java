package com.fittrack.whoop.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fittrack.whoop.config.WhoopProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Thin HTTP client for the WHOOP Developer API v2.
 *
 * <p>This is the only class that knows WHOOP's URLs, parameter names and pagination shape.
 * Everything above it deals in {@link WhoopPage} and {@link WhoopTokens}, so a change to WHOOP's
 * wire format is contained here.
 *
 * <p>Endpoints used (base {@code https://api.prod.whoop.com/developer}):
 * <ul>
 *   <li>{@code GET /v2/user/profile/basic}
 *   <li>{@code GET /v2/user/measurement/body}
 *   <li>{@code GET /v2/cycle}, {@code GET /v2/recovery}
 *   <li>{@code GET /v2/activity/sleep}, {@code GET /v2/activity/workout}
 * </ul>
 * Collections page with {@code start}, {@code end}, {@code limit} and {@code nextToken}, and
 * respond with {@code records} plus {@code next_token}.
 */
@Component
public class WhoopClient {

    private static final Logger log = LoggerFactory.getLogger(WhoopClient.class);
    /** Guardrail so a pagination bug cannot loop forever against the live API. */
    private static final int MAX_PAGES = 200;

    private final WhoopProperties properties;
    private final RestClient restClient;
    private final RestClient authClient;
    private final Clock clock;

    public WhoopClient(WhoopProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(10));
        requestFactory.setReadTimeout(properties.getTimeout());

        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .requestFactory(requestFactory)
                .build();
        this.authClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    // ------------------------------------------------------------------ OAuth

    public String buildAuthorizationUrl(String state) {
        // encode() matters: scopes are space separated and the redirect URI contains a colon and
        // slashes, none of which are legal unencoded in a query string.
        return UriComponentsBuilder.fromUriString(properties.getAuthorizeUrl())
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", properties.scopeString())
                .queryParam("state", state)
                .encode()
                .build()
                .toUriString();
    }

    public WhoopTokens exchangeAuthorizationCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", properties.getRedirectUri());
        return requestTokens(form, "authorization code exchange");
    }

    public WhoopTokens refreshTokens(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        // WHOOP re-issues a refresh token only when "offline" is requested again on refresh.
        form.add("scope", "offline");
        return requestTokens(form, "token refresh");
    }

    /**
     * WHOOP expects client credentials in the request body rather than a Basic auth header.
     */
    private WhoopTokens requestTokens(MultiValueMap<String, String> form, String operation) {
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        try {
            JsonNode response = authClient
                    .post()
                    .uri(properties.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.hasNonNull("access_token")) {
                throw new WhoopApiException(502, "WHOOP " + operation + " returned no access token");
            }

            long expiresIn = response.path("expires_in").asLong(3600);
            return new WhoopTokens(
                    response.path("access_token").asText(),
                    response.hasNonNull("refresh_token") ? response.path("refresh_token").asText() : null,
                    clock.instant().plusSeconds(expiresIn),
                    response.path("scope").asText(null));
        } catch (RestClientResponseException ex) {
            // The exception message deliberately excludes the body, which contains tokens.
            log.warn("WHOOP {} failed with status {}", operation, ex.getStatusCode().value());
            throw new WhoopApiException(
                    ex.getStatusCode().value(), "WHOOP " + operation + " failed");
        } catch (RuntimeException ex) {
            throw new WhoopApiException(502, "WHOOP " + operation + " could not be completed", ex);
        }
    }

    public void revokeAccess(String accessToken) {
        try {
            authClient
                    .delete()
                    .uri(properties.getRevokeUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.warn("WHOOP access revocation returned status {}", ex.getStatusCode().value());
        } catch (RuntimeException ex) {
            log.warn("WHOOP access revocation could not be completed");
        }
    }

    // ------------------------------------------------------------------- Data

    public JsonNode getProfile(String accessToken) {
        return get(accessToken, "/v2/user/profile/basic");
    }

    public JsonNode getBodyMeasurement(String accessToken) {
        return get(accessToken, "/v2/user/measurement/body");
    }

    public List<JsonNode> getCycles(String accessToken, Instant start, Instant end) {
        return collect(accessToken, "/v2/cycle", start, end);
    }

    public List<JsonNode> getRecoveries(String accessToken, Instant start, Instant end) {
        return collect(accessToken, "/v2/recovery", start, end);
    }

    public List<JsonNode> getSleeps(String accessToken, Instant start, Instant end) {
        return collect(accessToken, "/v2/activity/sleep", start, end);
    }

    public List<JsonNode> getWorkouts(String accessToken, Instant start, Instant end) {
        return collect(accessToken, "/v2/activity/workout", start, end);
    }

    /** Walks every page of a collection endpoint using WHOOP's {@code next_token} cursor. */
    private List<JsonNode> collect(String accessToken, String path, Instant start, Instant end) {
        List<JsonNode> records = new ArrayList<>();
        String nextToken = null;
        for (int page = 0; page < MAX_PAGES; page++) {
            WhoopPage current = getPage(accessToken, path, start, end, nextToken);
            records.addAll(current.records());
            if (!current.hasMore()) {
                return records;
            }
            nextToken = current.nextToken();
        }
        log.warn("Stopped paginating {} after {} pages", path, MAX_PAGES);
        return records;
    }

    public WhoopPage getPage(
            String accessToken, String path, Instant start, Instant end, String nextToken) {
        JsonNode response = get(accessToken, uri -> {
            uri.path(path)
                    .queryParam("limit", properties.getPageSize())
                    .queryParam("start", DateTimeFormatter.ISO_INSTANT.format(start))
                    .queryParam("end", DateTimeFormatter.ISO_INSTANT.format(end));
            if (nextToken != null && !nextToken.isBlank()) {
                uri.queryParam("nextToken", nextToken);
            }
            return uri.build();
        });

        List<JsonNode> records = new ArrayList<>();
        if (response != null && response.path("records").isArray()) {
            response.path("records").forEach(records::add);
        }
        String token = response == null || !response.hasNonNull("next_token")
                ? null
                : response.path("next_token").asText();
        return new WhoopPage(records, token);
    }

    private JsonNode get(String accessToken, String path) {
        return get(accessToken, uri -> uri.path(path).build());
    }

    private JsonNode get(
            String accessToken,
            java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI> uriFunction) {
        try {
            return restClient
                    .get()
                    .uri(uriFunction::apply)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            throw new WhoopApiException(
                    ex.getStatusCode().value(),
                    "WHOOP API request failed with status " + ex.getStatusCode().value());
        } catch (RuntimeException ex) {
            throw new WhoopApiException(502, "WHOOP API could not be reached", ex);
        }
    }
}
