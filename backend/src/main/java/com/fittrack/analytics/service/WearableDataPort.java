package com.fittrack.analytics.service;

import com.fittrack.analytics.dto.WearableDaySnapshot;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * The analytics module's view of wearable data. Analytics depends on this interface, and the
 * WHOOP module supplies the implementation - so domain code never reaches into an integration.
 */
public interface WearableDataPort {

    WearableDaySnapshot forDay(UUID userId, LocalDate date, ZoneId zone);

    /** One snapshot per day in [from, to], including days with no data. */
    List<WearableDaySnapshot> forRange(UUID userId, LocalDate from, LocalDate to, ZoneId zone);
}
