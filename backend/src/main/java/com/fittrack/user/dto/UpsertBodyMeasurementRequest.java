package com.fittrack.user.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record UpsertBodyMeasurementRequest(
        Instant recordedAt,
        @NotNull @DecimalMin("20.0") @DecimalMax("400.0") BigDecimal weightKg,
        @DecimalMin("1.0") @DecimalMax("70.0") BigDecimal bodyFatPercentage,
        @Size(max = 500) String note) {}
