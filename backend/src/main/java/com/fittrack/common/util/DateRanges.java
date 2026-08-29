package com.fittrack.common.util;

import com.fittrack.common.exception.BadRequestException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/** Helpers for turning user-supplied local dates into instants in the user's own time zone. */
public final class DateRanges {

    /** Guardrail so a single analytics query can never scan an unbounded history. */
    public static final long MAX_RANGE_DAYS = 400;

    private DateRanges() {}

    public static Instant startOfDay(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

    /** Exclusive upper bound: the start of the day after {@code date}. */
    public static Instant endOfDayExclusive(LocalDate date, ZoneId zone) {
        return date.plusDays(1).atStartOfDay(zone).toInstant();
    }

    public static void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both 'from' and 'to' dates are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' must not be after 'to'");
        }
        long days = ChronoUnit.DAYS.between(from, to);
        if (days > MAX_RANGE_DAYS) {
            throw new BadRequestException("Date range must not exceed " + MAX_RANGE_DAYS + " days");
        }
    }

    public static ZoneId parseZone(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(zoneId);
        } catch (RuntimeException ex) {
            throw new BadRequestException("Unknown time zone: " + zoneId);
        }
    }
}
