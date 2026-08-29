package com.fittrack.auth.service;

import com.fittrack.auth.domain.RefreshTokenRepository;
import java.time.Clock;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Removes refresh tokens that expired long enough ago to be useless for audit purposes. */
@Component
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);
    private static final Duration RETENTION_AFTER_EXPIRY = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public RefreshTokenCleanupJob(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "${fittrack.jobs.refresh-token-cleanup-cron:0 15 3 * * *}")
    @Transactional
    public void purgeExpiredTokens() {
        int removed = refreshTokenRepository.deleteExpired(clock.instant().minus(RETENTION_AFTER_EXPIRY));
        if (removed > 0) {
            log.info("Purged {} expired refresh tokens", removed);
        }
    }
}
