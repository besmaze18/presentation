package com.fittrack.ai.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The provider-independent shape every {@code FoodAnalysisService} implementation returns.
 *
 * <p>{@code confidence} is the model's own self-reported confidence in [0, 1] and
 * {@code assumptions} lists what it had to guess (cooking method, oil, portion size). Both are
 * shown to the user, because this result is a <em>proposal</em>: it never becomes a nutrition
 * entry without explicit confirmation.
 */
public record FoodAnalysisResult(
        String mealName,
        List<AnalyzedFoodItem> items,
        BigDecimal totalCalories,
        BigDecimal totalProteinG,
        BigDecimal totalCarbsG,
        BigDecimal totalFatG,
        BigDecimal totalFiberG,
        BigDecimal confidence,
        List<String> assumptions,
        String notes,
        String providerName,
        String model,
        /** The provider's unmodified JSON, retained so estimation accuracy can be measured later. */
        String rawResponseJson,
        Long latencyMillis,
        Long inputTokens,
        Long outputTokens) {}
