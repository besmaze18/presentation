package com.fittrack.auth.dto;

/** Internal carrier: the access token returned to the client plus the raw refresh token. */
public record AuthTokens(String accessToken, long expiresInSeconds, String refreshToken) {}
