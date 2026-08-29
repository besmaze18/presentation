package com.fittrack.whoop.domain;

import com.fittrack.common.domain.BaseEntity;
import com.fittrack.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A user's WHOOP authorisation.
 *
 * <p>Both tokens are stored encrypted (AES-GCM) and are never exposed through any API - the
 * frontend only ever learns the connection's status. {@code whoopUserId} is unique so the same
 * WHOOP account cannot be attached to two application accounts.
 */
@Entity
@Table(name = "whoop_connections")
public class WhoopConnection extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "whoop_user_id", nullable = false, unique = true)
    private Long whoopUserId;

    @Column(name = "whoop_email", length = 320)
    private String whoopEmail;

    @Column(name = "whoop_first_name", length = 120)
    private String whoopFirstName;

    @Column(name = "whoop_last_name", length = 120)
    private String whoopLastName;

    @Column(name = "access_token_encrypted", nullable = false, length = 2048)
    private String accessTokenEncrypted;

    @Column(name = "refresh_token_encrypted", length = 2048)
    private String refreshTokenEncrypted;

    @Column(name = "access_token_expires_at")
    private Instant accessTokenExpiresAt;

    @Column(name = "scopes", length = 500)
    private String scopes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private WhoopConnectionStatus status = WhoopConnectionStatus.CONNECTED;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    /**
     * The upper bound of the last successful sync window. The next incremental sync starts from
     * here minus a small overlap, because WHOOP scores sleep and recovery after the fact.
     */
    @Column(name = "last_synced_through")
    private Instant lastSyncedThrough;

    @Column(name = "last_sync_error", length = 500)
    private String lastSyncError;

    protected WhoopConnection() {}

    public WhoopConnection(User user, Long whoopUserId, Instant connectedAt) {
        this.user = user;
        this.whoopUserId = whoopUserId;
        this.connectedAt = connectedAt;
    }

    public boolean needsRefresh(Instant now, java.time.Duration skew) {
        return accessTokenExpiresAt == null || accessTokenExpiresAt.minus(skew).isBefore(now);
    }

    public boolean isUsable() {
        return status == WhoopConnectionStatus.CONNECTED;
    }

    public void recordSyncSuccess(Instant at, Instant syncedThrough) {
        this.lastSyncAt = at;
        this.lastSyncedThrough = syncedThrough;
        this.lastSyncError = null;
        this.status = WhoopConnectionStatus.CONNECTED;
    }

    public void recordSyncFailure(Instant at, String message) {
        this.lastSyncAt = at;
        this.lastSyncError = message == null ? null : message.substring(0, Math.min(500, message.length()));
    }

    /** Marks the grant as no longer usable, e.g. after WHOOP rejects the refresh token. */
    public void markReauthorisationRequired(String reason) {
        this.status = WhoopConnectionStatus.REAUTHORISATION_REQUIRED;
        this.lastSyncError = reason == null ? null : reason.substring(0, Math.min(500, reason.length()));
    }

    public User getUser() {
        return user;
    }

    public Long getWhoopUserId() {
        return whoopUserId;
    }

    public void setWhoopUserId(Long whoopUserId) {
        this.whoopUserId = whoopUserId;
    }

    public String getWhoopEmail() {
        return whoopEmail;
    }

    public void setWhoopEmail(String whoopEmail) {
        this.whoopEmail = whoopEmail;
    }

    public String getWhoopFirstName() {
        return whoopFirstName;
    }

    public void setWhoopFirstName(String whoopFirstName) {
        this.whoopFirstName = whoopFirstName;
    }

    public String getWhoopLastName() {
        return whoopLastName;
    }

    public void setWhoopLastName(String whoopLastName) {
        this.whoopLastName = whoopLastName;
    }

    public String getAccessTokenEncrypted() {
        return accessTokenEncrypted;
    }

    public void setAccessTokenEncrypted(String accessTokenEncrypted) {
        this.accessTokenEncrypted = accessTokenEncrypted;
    }

    public String getRefreshTokenEncrypted() {
        return refreshTokenEncrypted;
    }

    public void setRefreshTokenEncrypted(String refreshTokenEncrypted) {
        this.refreshTokenEncrypted = refreshTokenEncrypted;
    }

    public Instant getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    public void setAccessTokenExpiresAt(Instant accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public WhoopConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(WhoopConnectionStatus status) {
        this.status = status;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Instant getLastSyncAt() {
        return lastSyncAt;
    }

    public Instant getLastSyncedThrough() {
        return lastSyncedThrough;
    }

    public String getLastSyncError() {
        return lastSyncError;
    }
}
