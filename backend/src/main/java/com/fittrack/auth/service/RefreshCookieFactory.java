package com.fittrack.auth.service;

import com.fittrack.common.security.JwtProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Builds the HttpOnly refresh-token cookie. Keeping the refresh token out of JavaScript's reach
 * means an XSS bug cannot exfiltrate a long-lived credential.
 */
@Component
public class RefreshCookieFactory {

    private final JwtProperties properties;

    public RefreshCookieFactory(JwtProperties properties) {
        this.properties = properties;
    }

    public String cookieName() {
        return properties.getCookieName();
    }

    public ResponseCookie create(String rawRefreshToken) {
        return base(rawRefreshToken).maxAge(properties.getRefreshTokenTtl()).build();
    }

    public ResponseCookie expired() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(properties.getCookieName(), value)
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .path(properties.getCookiePath())
                .sameSite(properties.getCookieSameSite());
    }
}
