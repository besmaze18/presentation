package com.fittrack.whoop.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WHOOP Developer API configuration.
 *
 * <p>Defaults target the current v2 API: OAuth 2.0 authorization-code flow at
 * {@code /oauth/oauth2/auth} and {@code /oauth/oauth2/token}, with data under
 * {@code https://api.prod.whoop.com/developer/v2}.
 */
@ConfigurationProperties(prefix = "fittrack.whoop")
public class WhoopProperties {

    private boolean enabled = true;

    private String clientId;

    private String clientSecret;

    /** Must exactly match a redirect URI registered on the WHOOP developer dashboard. */
    private String redirectUri = "http://localhost:8080/api/whoop/callback";

    /** Where the browser is sent once the OAuth exchange completes. */
    private String appRedirectUri = "http://localhost:5173/settings";

    private String apiBaseUrl = "https://api.prod.whoop.com/developer";

    private String authorizeUrl = "https://api.prod.whoop.com/oauth/oauth2/auth";

    private String tokenUrl = "https://api.prod.whoop.com/oauth/oauth2/token";

    private String revokeUrl = "https://api.prod.whoop.com/developer/v2/user/access";

    /**
     * WHOOP mixes plural and singular scope names ("read:cycles" but "read:workout"); these are
     * the names the API actually accepts. "offline" is what yields a refresh token.
     */
    private List<String> scopes = List.of(
            "read:profile",
            "read:body_measurement",
            "read:cycles",
            "read:recovery",
            "read:sleep",
            "read:workout",
            "offline");

    /** How far back the first synchronisation reaches. */
    private int initialSyncDays = 90;

    /**
     * Incremental syncs re-request a short overlap window, because WHOOP scores a night's sleep
     * and recovery some time after the activity itself ends.
     */
    private Duration incrementalSyncOverlap = Duration.ofDays(2);

    /** Refresh the access token this long before it actually expires. */
    private Duration tokenRefreshSkew = Duration.ofMinutes(5);

    private Duration timeout = Duration.ofSeconds(30);

    /** How long an unused OAuth state parameter stays valid. */
    private Duration stateTtl = Duration.ofMinutes(10);

    private int pageSize = 25;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAppRedirectUri() {
        return appRedirectUri;
    }

    public void setAppRedirectUri(String appRedirectUri) {
        this.appRedirectUri = appRedirectUri;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
        this.authorizeUrl = authorizeUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getRevokeUrl() {
        return revokeUrl;
    }

    public void setRevokeUrl(String revokeUrl) {
        this.revokeUrl = revokeUrl;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public int getInitialSyncDays() {
        return initialSyncDays;
    }

    public void setInitialSyncDays(int initialSyncDays) {
        this.initialSyncDays = initialSyncDays;
    }

    public Duration getIncrementalSyncOverlap() {
        return incrementalSyncOverlap;
    }

    public void setIncrementalSyncOverlap(Duration incrementalSyncOverlap) {
        this.incrementalSyncOverlap = incrementalSyncOverlap;
    }

    public Duration getTokenRefreshSkew() {
        return tokenRefreshSkew;
    }

    public void setTokenRefreshSkew(Duration tokenRefreshSkew) {
        this.tokenRefreshSkew = tokenRefreshSkew;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getStateTtl() {
        return stateTtl;
    }

    public void setStateTtl(Duration stateTtl) {
        this.stateTtl = stateTtl;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isConfigured() {
        return enabled
                && clientId != null
                && !clientId.isBlank()
                && clientSecret != null
                && !clientSecret.isBlank();
    }

    public String scopeString() {
        return String.join(" ", scopes);
    }
}
