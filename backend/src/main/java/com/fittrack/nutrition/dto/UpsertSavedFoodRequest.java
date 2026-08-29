package com.fittrack.nutrition.dto;

import com.fittrack.nutrition.domain.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpsertSavedFoodRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 120) String brand,
        @NotNull @Positive @DecimalMin("0.001") BigDecimal servingQuantity,
        @NotBlank @Size(max = 32) String servingUnit,
        @NotNull @Valid MacrosDto macros,
        MealType defaultMealType) {}
