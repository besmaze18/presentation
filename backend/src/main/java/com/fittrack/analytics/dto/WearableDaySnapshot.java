package com.fittrack.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Wearable-derived figures for one day, in the shape analytics needs. Defining this here rather
 * than reusing a WHOOP entity keeps the analytics and dashboard code independent of any one
 * device vendor: a second wearable would supply the same record.
 *
 * <p>Every field is nullable - a day may have a workout but no recovery score, or sleep but no
 * completed cycle.
 */
public record WearableDaySnapshot(
        LocalDate date,
        Integer recoveryScore,
        BigDecimal restingHeartRate,
        BigDecimal hrvMilli,
        BigDecimal strain,
        /** Total day energy expenditure reported by the device, in kilocalories. */
        BigDecimal expenditureKcal,
        Long sleepDurationMillis,
        Long sleepNeedMillis,
        Integer sleepPerformancePercentage,
        int workoutCount) {

    public static WearableDaySnapshot empty(LocalDate date) {
        return new WearableDaySnapshot(date, null, null, null, null, null, null, null, null, 0);
    }

    public boolean hasAnyData() {
        return recoveryScore != null
                || strain != null
                || expenditureKcal != null
                || sleepDurationMillis != null
                || workoutCount > 0;
    }
}
