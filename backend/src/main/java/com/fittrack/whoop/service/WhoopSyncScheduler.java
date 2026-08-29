package com.fittrack.whoop.service;

import com.fittrack.whoop.config.WhoopProperties;
import com.fittrack.whoop.domain.WhoopConnection;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fittrack.whoop.domain.WhoopOAuthStateRepository;

/**
 * Periodic incremental synchronisation for every connected account.
 *
 * <p>Each connection is synced in its own transaction so one user's expired grant cannot stop the
 * others from updating. WHOOP also offers webhooks; polling on a schedule is the simpler MVP
 * choice and is what the incremental overlap window is designed around.
 */
@Component
public class WhoopSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(WhoopSyncScheduler.class);

    private final WhoopSyncService syncService;
    private final WhoopProperties properties;
    private final WhoopOAuthStateRepository stateRepository;
    private final Clock clock;

    public WhoopSyncScheduler(
            WhoopSyncService syncService,
            WhoopProperties properties,
            WhoopOAuthStateRepository stateRepository,
            Clock clock) {
        this.syncService = syncService;
        this.properties = properties;
        this.stateRepository = stateRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "${fittrack.jobs.whoop-sync-cron:0 0 * * * *}")
    public void syncAllConnections() {
        if (!properties.isConfigured()) {
            return;
        }
        List<WhoopConnection> connections = syncService.connectionsToSync();
        if (connections.isEmpty()) {
            return;
        }
        log.info("Starting scheduled WHOOP sync for {} connections", connections.size());

        int succeeded = 0;
        for (WhoopConnection connection : connections) {
            try {
                syncService.sync(connection.getUser().getId());
                succeeded++;
            } catch (RuntimeException ex) {
                // Failures are recorded on the connection; keep going for everyone else.
                log.warn(
                        "Scheduled WHOOP sync failed for user {}: {}",
                        connection.getUser().getId(),
                        ex.getMessage());
            }
        }
        log.info("Scheduled WHOOP sync finished: {}/{} succeeded", succeeded, connections.size());
    }

    /** Consumed and expired OAuth state rows have no value once they can no longer be used. */
    @Scheduled(cron = "${fittrack.jobs.whoop-state-cleanup-cron:0 30 3 * * *}")
    @Transactional
    public void purgeExpiredStates() {
        int removed = stateRepository.deleteExpired(clock.instant().minus(Duration.ofHours(1)));
        if (removed > 0) {
            log.debug("Purged {} expired WHOOP OAuth states", removed);
        }
    }
}
