package com.fittrack.nutrition.dto;

import com.fittrack.nutrition.domain.FoodEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record FoodEntryResponse(
        UUID id,
        String name,
        String mealType,
        Instant consumedAt,
        LocalDate entryDate,
        BigDecimal quantity,
        String unit,
        MacrosDto macros,
        String source,
        UUID aiAnalysisId,
        UUID savedFoodId,
        String notes,
        List<FoodItemDto> items,
        Instant createdAt) {

    public static FoodEntryResponse from(FoodEntry entry) {
        return new FoodEntryResponse(
                entry.getId(),
                entry.getName(),
                entry.getMealType().name(),
                entry.getConsumedAt(),
                entry.getEntryDate(),
                entry.getQuantity(),
                entry.getUnit(),
                MacrosDto.from(entry.getMacros()),
                entry.getSource().name(),
                entry.getAiAnalysisId(),
                entry.getSavedFoodId(),
                entry.getNotes(),
                entry.getItems().stream().map(FoodItemDto::from).toList(),
                entry.getCreatedAt());
    }
}
