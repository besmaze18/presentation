package com.fittrack.training.dto;

import com.fittrack.training.domain.TrainingSession;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TrainingSessionResponse(
        UUID id,
        String title,
        String category,
        String sportLabel,
        Instant startedAt,
        Instant endedAt,
        LocalDate sessionDate,
        int durationMinutes,
        Integer perceivedExertion,
        BigDecimal caloriesKcal,
        BigDecimal strain,
        Integer averageHeartRate,
        Integer maxHeartRate,
        BigDecimal distanceMeters,
        String notes,
        String source,
        /** Imported sessions accept notes and exertion but not edits to their measured fields. */
        boolean imported,
        List<ExerciseDto> exercises) {

    public static TrainingSessionResponse from(TrainingSession session) {
        return new TrainingSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getCategory().name(),
                session.getSportLabel(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getSessionDate(),
                session.getDurationMinutes(),
                session.getPerceivedExertion(),
                session.getCaloriesKcal(),
                session.getStrain(),
                session.getAverageHeartRate(),
                session.getMaxHeartRate(),
                session.getDistanceMeters(),
                session.getNotes(),
                session.getSource().name(),
                session.isImported(),
                session.getExercises().stream().map(ExerciseDto::from).toList());
    }

    /** Lighter projection used where exercise detail is not needed (dashboard, lists). */
    public static TrainingSessionResponse summary(TrainingSession session) {
        return new TrainingSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getCategory().name(),
                session.getSportLabel(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getSessionDate(),
                session.getDurationMinutes(),
                session.getPerceivedExertion(),
                session.getCaloriesKcal(),
                session.getStrain(),
                session.getAverageHeartRate(),
                session.getMaxHeartRate(),
                session.getDistanceMeters(),
                session.getNotes(),
                session.getSource().name(),
                session.isImported(),
                List.of());
    }
}
