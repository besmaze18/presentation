package com.fittrack.whoop.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.fittrack.common.domain.BaseEntity;
import com.fittrack.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A workout as WHOOP recorded it. Kept as its own row rather than only as a TrainingSession, so
 * the imported data stays intact even after the user edits the session it produced.
 */
@Entity
@Table(name = "whoop_workouts")
public class WhoopWorkout extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "whoop_workout_id", nullable = false, length = 64)
    private String whoopWorkoutId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @Column(name = "timezone_offset", length = 16)
    private String timezoneOffset;

    @Column(name = "sport_id")
    private Integer sportId;

    @Column(name = "sport_name", length = 80)
    private String sportName;

    @Column(name = "score_state", length = 32)
    private String scoreState;

    @Column(name = "strain", precision = 5, scale = 2)
    private BigDecimal strain;

    @Column(name = "kilojoules", precision = 10, scale = 2)
    private BigDecimal kilojoules;

    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;

    @Column(name = "distance_meters", precision = 12, scale = 3)
    private BigDecimal distanceMeters;

    @Column(name = "altitude_gain_meters", precision = 10, scale = 3)
    private BigDecimal altitudeGainMeters;

    @Column(name = "percent_recorded", precision = 6, scale = 2)
    private BigDecimal percentRecorded;

    /** The training session created from this workout, so a re-sync updates instead of duplicating. */
    @Column(name = "training_session_id")
    private UUID trainingSessionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload")
    private JsonNode rawPayload;

    protected WhoopWorkout() {}

    public WhoopWorkout(User user, String whoopWorkoutId) {
        this.user = user;
        this.whoopWorkoutId = whoopWorkoutId;
    }

    public User getUser() {
        return user;
    }

    public String getWhoopWorkoutId() {
        return whoopWorkoutId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public LocalDate getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(LocalDate workoutDate) {
        this.workoutDate = workoutDate;
    }

    public String getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(String timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public Integer getSportId() {
        return sportId;
    }

    public void setSportId(Integer sportId) {
        this.sportId = sportId;
    }

    public String getSportName() {
        return sportName;
    }

    public void setSportName(String sportName) {
        this.sportName = sportName;
    }

    public String getScoreState() {
        return scoreState;
    }

    public void setScoreState(String scoreState) {
        this.scoreState = scoreState;
    }

    public BigDecimal getStrain() {
        return strain;
    }

    public void setStrain(BigDecimal strain) {
        this.strain = strain;
    }

    public BigDecimal getKilojoules() {
        return kilojoules;
    }

    public void setKilojoules(BigDecimal kilojoules) {
        this.kilojoules = kilojoules;
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

    public BigDecimal getAltitudeGainMeters() {
        return altitudeGainMeters;
    }

    public void setAltitudeGainMeters(BigDecimal altitudeGainMeters) {
        this.altitudeGainMeters = altitudeGainMeters;
    }

    public BigDecimal getPercentRecorded() {
        return percentRecorded;
    }

    public void setPercentRecorded(BigDecimal percentRecorded) {
        this.percentRecorded = percentRecorded;
    }

    public UUID getTrainingSessionId() {
        return trainingSessionId;
    }

    public void setTrainingSessionId(UUID trainingSessionId) {
        this.trainingSessionId = trainingSessionId;
    }

    public JsonNode getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(JsonNode rawPayload) {
        this.rawPayload = rawPayload;
    }
}
