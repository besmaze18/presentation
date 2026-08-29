package com.fittrack.ai.dto;

import java.math.BigDecimal;

/**
 * Estimates macros for one named food at a given quantity - the narrow case where the user knows
 * exactly what they ate but not its nutritional content.
 */
public record NutritionEstimateRequest(String foodName, BigDecimal quantity, String unit) {}
