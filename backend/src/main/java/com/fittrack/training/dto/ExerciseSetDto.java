package com.fittrack.training.dto;

import com.fittrack.training.domain.ExerciseSet;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record ExerciseSetDto(
        @Min(0) @Max(1000) Integer repetitions,
        @DecimalMin("0.0") @DecimalMax("2000.0") BigDecimal weightKg,
        @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal rpe,
        @DecimalMin("0.0") BigDecimal distanceMeters,
        @Min(0) Integer durationSeconds) {

    public ExerciseSet toEntity() {
        return new ExerciseSet(repetitions, weightKg, rpe, distanceMeters, durationSeconds);
    }

    public static ExerciseSetDto from(ExerciseSet set) {
        return new ExerciseSetDto(
                set.getRepetitions(),
                set.getWeightKg(),
                set.getRpe(),
                set.getDistanceMeters(),
                set.getDurationSeconds());
    }
}
