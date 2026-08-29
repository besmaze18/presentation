package com.fittrack.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A dense day-by-day series for the history screen. The backend fills gaps so the client can plot
 * directly without reconstructing missing days, and aggregation stays in the database.
 */
public record AnalyticsSeriesResponse(
        LocalDate from, LocalDate to, String granularity, List<Bucket> buckets, Totals totals) {

    /** One bucket: a day, an ISO week, or a calendar month depending on the granularity. */
    public record Bucket(
            LocalDate date,
            String label,
            BigDecimal calories,
            BigDecimal proteinG,
            BigDecimal carbsG,
            BigDecimal fatG,
            BigDecimal fiberG,
            long foodEntryCount,
            BigDecimal weightKg,
            long workoutCount,
            long trainingMinutes,
            Integer recoveryScore,
            BigDecimal strain,
            Long sleepDurationMillis) {}

    public record Totals(
            BigDecimal averageCalories,
            BigDecimal averageProteinG,
            BigDecimal averageCarbsG,
            BigDecimal averageFatG,
            BigDecimal averageFiberG,
            long totalWorkouts,
            long totalTrainingMinutes,
            BigDecimal averageRecovery,
            BigDecimal averageStrain,
            BigDecimal averageSleepHours,
            BigDecimal weightChangeKg,
            /** Days in the range that have at least one logged food entry. */
            long daysLogged) {}
}
