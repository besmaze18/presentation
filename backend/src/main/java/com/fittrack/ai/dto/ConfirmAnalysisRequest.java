package com.fittrack.ai.dto;

import com.fittrack.nutrition.domain.MealType;
import com.fittrack.nutrition.dto.FoodItemDto;
import com.fittrack.nutrition.dto.MacrosDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

/**
 * The user's reviewed and possibly corrected version of an AI prediction. The values sent here -
 * not the prediction - are what get saved as nutrition.
 */
public record ConfirmAnalysisRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull MealType mealType,
        Instant consumedAt,
        @Valid @Size(max = 40) List<FoodItemDto> items,
        @Valid MacrosDto macros,
        @Size(max = 1000) String notes) {}
