package com.fittrack.training.dto;

import com.fittrack.training.domain.Exercise;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ExerciseDto(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 500) String notes,
        @Valid @Size(max = 50) List<ExerciseSetDto> sets) {

    public Exercise toEntity() {
        Exercise exercise = new Exercise(name.trim(), notes);
        exercise.replaceSets(
                sets == null ? List.of() : sets.stream().map(ExerciseSetDto::toEntity).toList());
        return exercise;
    }

    public static ExerciseDto from(Exercise exercise) {
        return new ExerciseDto(
                exercise.getName(),
                exercise.getNotes(),
                exercise.getSets().stream().map(ExerciseSetDto::from).toList());
    }
}
