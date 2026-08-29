package com.fittrack.whoop.client;

import java.time.Instant;

/** An OAuth token set as WHOOP returns it. Never logged and never sent to the frontend. */
public record WhoopTokens(
        String accessToken, String refreshToken, Instant expiresAt, String scope) {}
