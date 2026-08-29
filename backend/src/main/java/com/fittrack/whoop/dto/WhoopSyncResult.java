package com.fittrack.whoop.dto;

import java.time.Instant;

/** What a synchronisation run imported. Counts distinguish new records from updated ones. */
public record WhoopSyncResult(
        Instant syncedAt,
        Instant windowStart,
        Instant windowEnd,
        int cyclesImported,
        int cyclesUpdated,
        int recoveriesImported,
        int recoveriesUpdated,
        int sleepsImported,
        int sleepsUpdated,
        int workoutsImported,
        int workoutsUpdated,
        int trainingSessionsCreated,
        int bodyMeasurementsImported,
        boolean initialSync) {

    public int totalImported() {
        return cyclesImported + recoveriesImported + sleepsImported + workoutsImported;
    }

    public int totalUpdated() {
        return cyclesUpdated + recoveriesUpdated + sleepsUpdated + workoutsUpdated;
    }
}
