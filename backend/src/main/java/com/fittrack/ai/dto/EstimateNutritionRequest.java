package com.fittrack.ai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EstimateNutritionRequest(
        @NotBlank @Size(max = 200) String foodName,
        @DecimalMin("0.0") BigDecimal quantity,
        @Size(max = 32) String unit) {}
