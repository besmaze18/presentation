package com.fittrack.support;

import com.fittrack.ai.dto.AnalyzedFoodItem;
import com.fittrack.ai.dto.FoodAnalysisResult;
import com.fittrack.ai.dto.ImageAnalysisRequest;
import com.fittrack.ai.dto.NutritionEstimateRequest;
import com.fittrack.ai.dto.TextAnalysisRequest;
import com.fittrack.ai.service.AiUnavailableException;
import com.fittrack.ai.service.FoodAnalysisService;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stands in for a real model provider. Tests drive the workflow without a network call, and can
 * make the provider fail on demand to exercise graceful degradation.
 */
public class StubFoodAnalysisService implements FoodAnalysisService {

    public static final String RAW_JSON =
            """
            {"meal_name":"Chicken, rice and olive oil",
             "items":[
               {"name":"Chicken breast","quantity":300,"unit":"g","calories":495,"protein_g":93,
                "carbs_g":0,"fat_g":11,"fiber_g":0},
               {"name":"Basmati rice, cooked","quantity":200,"unit":"g","calories":260,"protein_g":5.2,
                "carbs_g":56,"fat_g":0.6,"fiber_g":0.8},
               {"name":"Olive oil","quantity":15,"unit":"g","calories":133,"protein_g":0,
                "carbs_g":0,"fat_g":15,"fiber_g":0}],
             "confidence":0.78,
             "assumptions":["Chicken was grilled without added fat","Rice measured after cooking"],
             "notes":"Portion sizes were taken from the description."}
            """;

    private final AtomicInteger textCalls = new AtomicInteger();
    private final AtomicInteger imageCalls = new AtomicInteger();

    private volatile boolean available = true;
    private volatile RuntimeException failure;

    public void failWith(RuntimeException exception) {
        this.failure = exception;
    }

    public void reset() {
        this.failure = null;
        this.available = true;
        textCalls.set(0);
        imageCalls.set(0);
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int textCalls() {
        return textCalls.get();
    }

    public int imageCalls() {
        return imageCalls.get();
    }

    @Override
    public String providerName() {
        return "stub";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public FoodAnalysisResult analyzeText(TextAnalysisRequest request) {
        textCalls.incrementAndGet();
        return result();
    }

    @Override
    public FoodAnalysisResult analyzeImage(ImageAnalysisRequest request) {
        imageCalls.incrementAndGet();
        return result();
    }

    @Override
    public FoodAnalysisResult estimateNutrition(NutritionEstimateRequest request) {
        return result();
    }

    private FoodAnalysisResult result() {
        if (failure != null) {
            throw failure;
        }
        if (!available) {
            throw new AiUnavailableException("AI analysis is not configured on this instance");
        }
        return new FoodAnalysisResult(
                "Chicken, rice and olive oil",
                List.of(
                        new AnalyzedFoodItem(
                                "Chicken breast", new BigDecimal("300"), "g",
                                new BigDecimal("495"), new BigDecimal("93"), BigDecimal.ZERO,
                                new BigDecimal("11"), BigDecimal.ZERO),
                        new AnalyzedFoodItem(
                                "Basmati rice, cooked", new BigDecimal("200"), "g",
                                new BigDecimal("260"), new BigDecimal("5.2"), new BigDecimal("56"),
                                new BigDecimal("0.6"), new BigDecimal("0.8")),
                        new AnalyzedFoodItem(
                                "Olive oil", new BigDecimal("15"), "g",
                                new BigDecimal("133"), BigDecimal.ZERO, BigDecimal.ZERO,
                                new BigDecimal("15"), BigDecimal.ZERO)),
                new BigDecimal("888"),
                new BigDecimal("98.2"),
                new BigDecimal("56"),
                new BigDecimal("26.6"),
                new BigDecimal("0.8"),
                new BigDecimal("0.78"),
                List.of("Chicken was grilled without added fat", "Rice measured after cooking"),
                "Portion sizes were taken from the description.",
                "stub",
                "stub-model-1",
                RAW_JSON,
                123L,
                1200L,
                340L);
    }
}
