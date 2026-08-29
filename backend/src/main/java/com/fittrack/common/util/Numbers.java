package com.fittrack.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Rounding helpers so every calculated figure is presented with a consistent precision. */
public final class Numbers {

    private Numbers() {}

    public static BigDecimal scale(BigDecimal value, int decimals) {
        return value == null ? null : value.setScale(decimals, RoundingMode.HALF_UP);
    }

    public static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** Rounds to one decimal place - the precision used for macro grams and body weight. */
    public static BigDecimal oneDecimal(BigDecimal value) {
        return scale(value, 1);
    }

    public static BigDecimal ofDouble(Double value, int decimals) {
        return value == null ? null : scale(BigDecimal.valueOf(value), decimals);
    }

    public static Integer roundToInt(BigDecimal value) {
        return value == null ? null : value.setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
