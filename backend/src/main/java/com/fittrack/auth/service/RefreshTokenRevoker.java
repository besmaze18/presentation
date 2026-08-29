package com.fittrack.auth.service;

import com.fittrack.auth.domain.RefreshTokenRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revokes refresh tokens in a transaction of its own.
 *
 * <p>This exists because of a subtle and dangerous interaction: reuse detection revokes a token
 * family and then rejects the request by throwing. If the revocation shared the caller's
 * transaction, that throw would mark the transaction rollback-only and quietly undo the very
 * revocation the theft response depends on — a stolen token would keep working.
 *
 * <p>{@code REQUIRES_NEW} makes the revocation commit independently of whether the request that
 * triggered it succeeds. It must live on a separate bean: a self-invoked method would bypass the
 * proxy and inherit the caller's transaction again.
 */
@Component
public class RefreshTokenRevoker {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public RefreshTokenRevoker(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamily(UUID familyId) {
        refreshTokenRepository.revokeFamily(familyId, clock.instant());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllForUser(userId, clock.instant());
    }
}
