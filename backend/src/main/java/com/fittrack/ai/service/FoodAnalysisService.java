package com.fittrack.ai.service;

import com.fittrack.ai.dto.FoodAnalysisResult;
import com.fittrack.ai.dto.ImageAnalysisRequest;
import com.fittrack.ai.dto.NutritionEstimateRequest;
import com.fittrack.ai.dto.TextAnalysisRequest;

/**
 * Provider-independent food analysis.
 *
 * <p>Nothing outside {@code com.fittrack.ai.provider} knows which model or vendor is behind this
 * interface. Implementations must return the shared {@link FoodAnalysisResult} shape and must
 * validate the provider's output before returning it - a malformed or implausible response is an
 * {@code AiUnavailableException}, never a half-populated result that could reach the database.
 */
public interface FoodAnalysisService {

    String providerName();

    /** False when the provider is disabled or unconfigured, so callers can offer manual entry. */
    boolean isAvailable();

    FoodAnalysisResult analyzeText(TextAnalysisRequest request);

    FoodAnalysisResult analyzeImage(ImageAnalysisRequest request);

    FoodAnalysisResult estimateNutrition(NutritionEstimateRequest request);
}
