package com.fittrack.training.domain;

import com.fittrack.common.domain.BaseEntity;
import com.fittrack.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * One training session, whether entered by hand or imported from a wearable.
 *
 * <p>The session owns an ordered list of exercises, each owning its own sets. Nothing in V1's UI
 * requires that depth, but modelling it now means exercise-level strength tracking can be added
 * without reshaping the aggregate or migrating existing sessions.
 *
 * <p>Wearable-derived metrics (strain, heart rates, distance) live alongside the user's own
 * inputs rather than replacing them: an imported workout still accepts notes and a perceived
 * exertion rating.
 */
@Entity
@Table(name = "training_sessions")
public class TrainingSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 24)
    private TrainingCategory category = TrainingCategory.OTHER;

    /** Free-text sport label, used to preserve a wearable's own naming (e.g. "Weightlifting"). */
    @Column(name = "sport_label", length = 80)
    private String sportLabel;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    /** Calendar date in the user's time zone, denormalised for day and range queries. */
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    /** RPE on the usual 1-10 scale. Null when not rated. */
    @Column(name = "perceived_exertion")
    private Integer perceivedExertion;

    @Column(name = "calories_kcal", precision = 8, scale = 2)
    private BigDecimal caloriesKcal;

    @Column(name = "strain", precision = 5, scale = 2)
    private BigDecimal strain;

    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;

    @Column(name = "distance_meters", precision = 10, scale = 2)
    private BigDecimal distanceMeters;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 24)
    private TrainingSource source = TrainingSource.MANUAL;

    /** Wearable identifier, kept unique per user so re-syncing never duplicates a workout. */
    @Column(name = "external_id", length = 128)
    private String externalId;

    @OneToMany(
            mappedBy = "trainingSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<Exercise> exercises = new ArrayList<>();

    protected TrainingSession() {}

    public TrainingSession(User user, String title, Instant startedAt, LocalDate sessionDate) {
        this.user = user;
        this.title = title;
        this.startedAt = startedAt;
        this.sessionDate = sessionDate;
    }

    public void replaceExercises(List<Exercise> newExercises) {
        exercises.clear();
        int position = 0;
        for (Exercise exercise : newExercises) {
            exercise.attachTo(this, position++);
            exercises.add(exercise);
        }
    }

    public boolean isImported() {
        return source != TrainingSource.MANUAL;
    }

    public User getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TrainingCategory getCategory() {
        return category;
    }

    public void setCategory(TrainingCategory category) {
        this.category = category;
    }

    public String getSportLabel() {
        return sportLabel;
    }

    public void setSportLabel(String sportLabel) {
        this.sportLabel = sportLabel;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getPerceivedExertion() {
        return perceivedExertion;
    }

    public void setPerceivedExertion(Integer perceivedExertion) {
        this.perceivedExertion = perceivedExertion;
    }

    public BigDecimal getCaloriesKcal() {
        return caloriesKcal;
    }

    public void setCaloriesKcal(BigDecimal caloriesKcal) {
        this.caloriesKcal = caloriesKcal;
    }

    public BigDecimal getStrain() {
        return strain;
    }

    public void setStrain(BigDecimal strain) {
        this.strain = strain;
    }

    public Integer getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(Integer averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }

    public Integer getMaxHeartRate() {
        return maxHeartRate;
    }

    public void setMaxHeartRate(Integer maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public BigDecimal getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(BigDecimal distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public TrainingSource getSource() {
        return source;
    }

    public void setSource(TrainingSource source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }
}
