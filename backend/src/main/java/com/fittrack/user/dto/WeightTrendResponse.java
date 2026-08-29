package com.fittrack.user.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Weight summary shown on the dashboard and progress screen. Averages smooth day-to-day water
 * fluctuation, so the deltas compare <em>averages</em> rather than two single readings.
 */
public record WeightTrendResponse(
        BigDecimal latestKg,
        Instant latestRecordedAt,
        BigDecimal average7dKg,
        BigDecimal average30dKg,
        /** Change in the 7-day average versus the preceding 7 days. */
        BigDecimal change7dKg,
        /** Change in the 30-day average versus the preceding 30 days. */
        BigDecimal change30dKg,
        BigDecimal targetKg,
        BigDecimal toTargetKg) {}
