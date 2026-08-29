package com.fittrack.user.domain;

import java.math.BigDecimal;

/**
 * Conventional Harris-Benedict style activity multipliers applied to BMR to derive a baseline
 * TDEE estimate. Used only when no wearable expenditure data is available for a day.
 */
public enum ActivityLevel {
    SEDENTARY(new BigDecimal("1.20")),
    LIGHT(new BigDecimal("1.375")),
    MODERATE(new BigDecimal("1.55")),
    ACTIVE(new BigDecimal("1.725")),
    VERY_ACTIVE(new BigDecimal("1.90"));

    private final BigDecimal multiplier;

    ActivityLevel(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    public BigDecimal multiplier() {
        return multiplier;
    }
}
