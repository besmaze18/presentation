package com.fittrack.training.domain;

import com.fittrack.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** One set: reps and load for strength work, or distance and time for conditioning. */
@Entity
@Table(name = "exercise_sets")
public class ExerciseSet extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "repetitions")
    private Integer repetitions;

    @Column(name = "weight_kg", precision = 7, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "rpe", precision = 3, scale = 1)
    private BigDecimal rpe;

    @Column(name = "distance_meters", precision = 10, scale = 2)
    private BigDecimal distanceMeters;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    protected ExerciseSet() {}

    public ExerciseSet(
            Integer repetitions,
            BigDecimal weightKg,
            BigDecimal rpe,
            BigDecimal distanceMeters,
            Integer durationSeconds) {
        this.repetitions = repetitions;
        this.weightKg = weightKg;
        this.rpe = rpe;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
    }

    void attachTo(Exercise exercise, int position) {
        this.exercise = exercise;
        this.position = position;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public int getPosition() {
        return position;
    }

    public Integer getRepetitions() {
        return repetitions;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public BigDecimal getRpe() {
        return rpe;
    }

    public BigDecimal getDistanceMeters() {
        return distanceMeters;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }
}
