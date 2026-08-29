package com.fittrack.nutrition.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregated nutrition for one calendar day, produced by the database rather than by summing in
 * the browser. Fields are nullable-safe: a day with no entries never reaches this record.
 */
public record DailyMacroTotals(
        LocalDate date,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG,
        BigDecimal fiberG,
        long entryCount) {

    public static DailyMacroTotals empty(LocalDate date) {
        return new DailyMacroTotals(
                date,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0);
    }
}
