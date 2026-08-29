package com.fittrack.training.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * The subset of fields editable on an imported session. Measured values (strain, heart rate,
 * duration) stay as the device reported them so a re-sync never fights the user.
 */
public record AnnotateTrainingSessionRequest(
        @Size(max = 200) String title,
        com.fittrack.training.domain.TrainingCategory category,
        @Min(1) @Max(10) Integer perceivedExertion,
        @Size(max = 2000) String notes) {}
