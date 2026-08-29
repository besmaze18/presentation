package com.fittrack.nutrition.dto;

import com.fittrack.nutrition.domain.SavedFood;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SavedFoodResponse(
        UUID id,
        String name,
        String brand,
        BigDecimal servingQuantity,
        String servingUnit,
        MacrosDto macros,
        String defaultMealType,
        long usageCount,
        Instant lastUsedAt) {

    public static SavedFoodResponse from(SavedFood food) {
        return new SavedFoodResponse(
                food.getId(),
                food.getName(),
                food.getBrand(),
                food.getServingQuantity(),
                food.getServingUnit(),
                MacrosDto.from(food.getMacros()),
                food.getDefaultMealType() == null ? null : food.getDefaultMealType().name(),
                food.getUsageCount(),
                food.getLastUsedAt());
    }
}
