package com.fittrack.auth.dto;

import com.fittrack.user.dto.UserResponse;

/**
 * The access token is short-lived and held in memory by the SPA. The refresh token is never in
 * this payload - it is delivered as an HttpOnly cookie scoped to /api/auth.
 */
public record AuthResponse(
        String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {

    public static AuthResponse of(AuthTokens tokens, UserResponse user) {
        return new AuthResponse(tokens.accessToken(), "Bearer", tokens.expiresInSeconds(), user);
    }
}
