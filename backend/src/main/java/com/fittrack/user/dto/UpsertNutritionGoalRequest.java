package com.fittrack.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Targets take effect from a chosen date, which is what lets a user change goals without
 * retroactively rewriting how past days were scored.
 */
public record UpsertNutritionGoalRequest(
        LocalDate effectiveFrom,
        @NotNull @Min(800) @Max(12000) Integer calorieTarget,
        @NotNull @DecimalMin("0.0") @DecimalMax("800.0") BigDecimal proteinTargetG,
        @NotNull @DecimalMin("0.0") @DecimalMax("1500.0") BigDecimal carbsTargetG,
        @NotNull @DecimalMin("0.0") @DecimalMax("600.0") BigDecimal fatTargetG,
        @NotNull @DecimalMin("0.0") @DecimalMax("300.0") BigDecimal fiberTargetG,
        @DecimalMin("20.0") @DecimalMax("400.0") BigDecimal targetBodyWeightKg) {}
