package com.fittrack.whoop.service;

import com.fittrack.whoop.domain.WhoopConnectionRepository;
import java.time.Clock;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records why a WHOOP operation failed, in a transaction of its own.
 *
 * <p>Both callers note the failure on the connection and then rethrow so the request fails. Sharing
 * the caller's transaction would mark it rollback-only and discard the note, so the user would
 * never learn that their grant needs re-authorising and the sync would keep failing silently.
 * {@code REQUIRES_NEW} on a separate bean is what makes the record survive the rethrow.
 */
@Component
public class WhoopConnectionStateRecorder {

    private final WhoopConnectionRepository connectionRepository;
    private final Clock clock;

    public WhoopConnectionStateRecorder(
            WhoopConnectionRepository connectionRepository, Clock clock) {
        this.connectionRepository = connectionRepository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(UUID connectionId, String message, boolean needsReauthorisation) {
        connectionRepository.findById(connectionId).ifPresent(connection -> {
            connection.recordSyncFailure(clock.instant(), message);
            if (needsReauthorisation) {
                connection.markReauthorisationRequired(message);
            }
            connectionRepository.save(connection);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markReauthorisationRequired(UUID connectionId, String reason) {
        connectionRepository.findById(connectionId).ifPresent(connection -> {
            connection.markReauthorisationRequired(reason);
            connectionRepository.save(connection);
        });
    }

    // Deliberately no overload taking the entity: calling the annotated method on `this` would
    // bypass the proxy and silently rejoin the caller's transaction - the exact failure this
    // class exists to prevent. Callers pass the id.
}
