package com.fittrack.whoop.domain;

import com.fittrack.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A single-use OAuth {@code state} value.
 *
 * <p>The callback arrives on an unauthenticated browser redirect, so state does double duty: it
 * is the CSRF defence <em>and</em> the only way to know which user started the flow. Rows are
 * short-lived and consumed on first use, so a captured callback URL cannot be replayed.
 */
@Entity
@Table(name = "whoop_oauth_states")
public class WhoopOAuthState extends BaseEntity {

    @Column(name = "state", nullable = false, unique = true, length = 128)
    private String state;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected WhoopOAuthState() {}

    public WhoopOAuthState(String state, UUID userId, Instant expiresAt) {
        this.state = state;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return consumedAt == null && expiresAt.isAfter(now);
    }

    public void consume(Instant now) {
        this.consumedAt = now;
    }

    public String getState() {
        return state;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }
}
