package com.fittrack.ai.dto;

import java.math.BigDecimal;

/** One food the model believes it identified, with its estimated portion and macros. */
public record AnalyzedFoodItem(
        String name,
        BigDecimal quantity,
        String unit,
        BigDecimal calories,
        BigDecimal proteinG,
        BigDecimal carbsG,
        BigDecimal fatG,
        BigDecimal fiberG) {}
