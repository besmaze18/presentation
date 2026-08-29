package com.fittrack.whoop.dto;

import com.fittrack.whoop.domain.WhoopConnection;
import java.time.Instant;

/**
 * What the frontend is allowed to know about a WHOOP connection.
 *
 * <p>Deliberately contains no tokens, no client id and no client secret - those never leave the
 * server.
 */
public record WhoopConnectionStatusResponse(
        boolean configured,
        boolean connected,
        String status,
        String whoopFirstName,
        String whoopLastName,
        Instant connectedAt,
        Instant lastSyncAt,
        String lastSyncError,
        long cycleCount,
        long recoveryCount,
        long sleepCount,
        long workoutCount) {

    public static WhoopConnectionStatusResponse notConfigured() {
        return new WhoopConnectionStatusResponse(
                false, false, "NOT_CONFIGURED", null, null, null, null, null, 0, 0, 0, 0);
    }

    public static WhoopConnectionStatusResponse notConnected() {
        return new WhoopConnectionStatusResponse(
                true, false, "NOT_CONNECTED", null, null, null, null, null, 0, 0, 0, 0);
    }

    public static WhoopConnectionStatusResponse of(
            WhoopConnection connection,
            long cycles,
            long recoveries,
            long sleeps,
            long workouts) {
        return new WhoopConnectionStatusResponse(
                true,
                connection.isUsable(),
                connection.getStatus().name(),
                connection.getWhoopFirstName(),
                connection.getWhoopLastName(),
                connection.getConnectedAt(),
                connection.getLastSyncAt(),
                connection.getLastSyncError(),
                cycles,
                recoveries,
                sleeps,
                workouts);
    }
}
