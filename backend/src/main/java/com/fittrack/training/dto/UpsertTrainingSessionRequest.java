package com.fittrack.training.dto;

import com.fittrack.training.domain.TrainingCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record UpsertTrainingSessionRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull TrainingCategory category,
        @Size(max = 80) String sportLabel,
        @NotNull Instant startedAt,
        @NotNull @Min(1) @Max(1440) Integer durationMinutes,
        @Min(1) @Max(10) Integer perceivedExertion,
        @DecimalMin("0.0") BigDecimal caloriesKcal,
        @DecimalMin("0.0") BigDecimal distanceMeters,
        @Size(max = 2000) String notes,
        @Valid @Size(max = 60) List<ExerciseDto> exercises) {}
