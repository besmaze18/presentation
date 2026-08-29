package com.fittrack.nutrition.dto;

import com.fittrack.nutrition.domain.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Manual macro entry. When {@code items} is supplied the entry totals are recomputed from the
 * items, so a multi-component meal can never drift out of step with its parts.
 */
public record CreateFoodEntryRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull MealType mealType,
        Instant consumedAt,
        @DecimalMin("0.0") BigDecimal quantity,
        @Size(max = 32) String unit,
        @Valid MacrosDto macros,
        @Valid @Size(max = 40) List<FoodItemDto> items,
        @Size(max = 1000) String notes,
        UUID aiAnalysisId) {}
