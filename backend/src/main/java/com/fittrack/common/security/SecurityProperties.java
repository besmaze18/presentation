package com.fittrack.common.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fittrack.security")
public class SecurityProperties {

    /** Origins permitted to call the API from a browser. Set via APP_CORS_ORIGINS. */
    private List<String> corsAllowedOrigins = List.of("http://localhost:5173");

    private boolean registrationEnabled = true;

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public boolean isRegistrationEnabled() {
        return registrationEnabled;
    }

    public void setRegistrationEnabled(boolean registrationEnabled) {
        this.registrationEnabled = registrationEnabled;
    }
}
