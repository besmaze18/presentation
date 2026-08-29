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

public record UpdateFoodEntryRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull MealType mealType,
        @NotNull Instant consumedAt,
        @DecimalMin("0.0") BigDecimal quantity,
        @Size(max = 32) String unit,
        @Valid MacrosDto macros,
        @Valid @Size(max = 40) List<FoodItemDto> items,
        @Size(max = 1000) String notes) {}
