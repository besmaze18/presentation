package com.fittrack.analytics.service;

import com.fittrack.analytics.dto.WearableDaySnapshot;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

/**
 * Fallback used when no wearable integration is active. Keeps the dashboard fully functional -
 * every wearable field simply reads as "no data" instead of the endpoint failing.
 */
@Configuration
public class NoWearableDataAdapter {

    @org.springframework.context.annotation.Bean
    @ConditionalOnMissingBean(WearableDataPort.class)
    public WearableDataPort emptyWearableDataPort() {
        return new WearableDataPort() {
            @Override
            public WearableDaySnapshot forDay(UUID userId, LocalDate date, ZoneId zone) {
                return WearableDaySnapshot.empty(date);
            }

            @Override
            public List<WearableDaySnapshot> forRange(
                    UUID userId, LocalDate from, LocalDate to, ZoneId zone) {
                return Stream.iterate(from, date -> !date.isAfter(to), date -> date.plusDays(1))
                        .map(WearableDaySnapshot::empty)
                        .toList();
            }
        };
    }

    @org.springframework.context.annotation.Bean
    @ConditionalOnMissingBean(WearableConnectionStatusPort.class)
    public WearableConnectionStatusPort noWearableConnectionStatusPort() {
        return userId -> false;
    }
}
