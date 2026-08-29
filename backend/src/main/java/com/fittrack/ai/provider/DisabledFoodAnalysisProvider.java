package com.fittrack.ai.provider;

import com.fittrack.ai.dto.FoodAnalysisResult;
import com.fittrack.ai.dto.ImageAnalysisRequest;
import com.fittrack.ai.dto.NutritionEstimateRequest;
import com.fittrack.ai.dto.TextAnalysisRequest;
import com.fittrack.ai.service.AiUnavailableException;
import com.fittrack.ai.service.FoodAnalysisService;

/**
 * Active when no AI provider is configured. Every call fails cleanly with a 503 so the client
 * offers manual entry instead - a missing credential must never take the rest of the app down.
 */
public class DisabledFoodAnalysisProvider implements FoodAnalysisService {

    private static final String MESSAGE =
            "AI food analysis is not configured on this instance. Enter the meal manually instead.";

    @Override
    public String providerName() {
        return "disabled";
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public FoodAnalysisResult analyzeText(TextAnalysisRequest request) {
        throw new AiUnavailableException(MESSAGE);
    }

    @Override
    public FoodAnalysisResult analyzeImage(ImageAnalysisRequest request) {
        throw new AiUnavailableException(MESSAGE);
    }

    @Override
    public FoodAnalysisResult estimateNutrition(NutritionEstimateRequest request) {
        throw new AiUnavailableException(MESSAGE);
    }
}
