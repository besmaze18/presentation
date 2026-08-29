package com.fittrack.analytics.dto;

import java.math.BigDecimal;

/**
 * The four energy figures are kept separate on purpose. A wearable's reported burn is not
 * treated as ground-truth TDEE: it is reported alongside the conventional estimate, and
 * {@code balanceBasis} names which figure the balance was computed against.
 */
public record EnergySummary(
        BigDecimal intakeKcal,
        /** Reported by the connected wearable, when it has data for the day. */
        BigDecimal wearableExpenditureKcal,
        /** Mifflin-St Jeor estimate. Null when height, birth date or weight is unknown. */
        BigDecimal estimatedBmrKcal,
        /** BMR scaled by the configured activity level. An estimate, not a measurement. */
        BigDecimal estimatedTdeeKcal,
        BigDecimal balanceKcal,
        BalanceBasis balanceBasis,
        BigDecimal weightKg) {

    public enum BalanceBasis {
        /** Balance computed against the wearable's reported expenditure. */
        WEARABLE_EXPENDITURE,
        /** Balance computed against the conventional TDEE estimate. */
        ESTIMATED_TDEE,
        /** Neither figure was available, so no balance is reported. */
        NONE
    }
}
