package com.fittrack.nutrition.dto;

import com.fittrack.nutrition.domain.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Logs a saved food. {@code quantity} is expressed in the saved food's own serving unit; the
 * stored macros are scaled by quantity / servingQuantity.
 */
public record LogSavedFoodRequest(
        @NotNull @Positive @DecimalMin("0.001") BigDecimal quantity,
        MealType mealType,
        Instant consumedAt) {}
