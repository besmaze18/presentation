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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * A recovery score. In v2 a recovery belongs to a cycle and references the sleep it was computed
 * from by UUID; the cycle id is the natural key for idempotent upserts.
 */
@Entity
@Table(name = "whoop_recoveries")
public class WhoopRecovery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "whoop_cycle_id", nullable = false)
    private Long whoopCycleId;

    @Column(name = "whoop_sleep_id", length = 64)
    private String whoopSleepId;

    @Column(name = "recovery_date", nullable = false)
    private LocalDate recoveryDate;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "score_state", length = 32)
    private String scoreState;

    @Column(name = "user_calibrating")
    private Boolean userCalibrating;

    @Column(name = "recovery_score")
    private Integer recoveryScore;

    @Column(name = "resting_heart_rate", precision = 6, scale = 2)
    private BigDecimal restingHeartRate;

    @Column(name = "hrv_rmssd_milli", precision = 8, scale = 3)
    private BigDecimal hrvRmssdMilli;

    @Column(name = "spo2_percentage", precision = 5, scale = 2)
    private BigDecimal spo2Percentage;

    @Column(name = "skin_temp_celsius", precision = 5, scale = 2)
    private BigDecimal skinTempCelsius;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload")
    private JsonNode rawPayload;

    protected WhoopRecovery() {}

    public WhoopRecovery(User user, Long whoopCycleId) {
        this.user = user;
        this.whoopCycleId = whoopCycleId;
    }

    public User getUser() {
        return user;
    }

    public Long getWhoopCycleId() {
        return whoopCycleId;
    }

    public String getWhoopSleepId() {
        return whoopSleepId;
    }

    public void setWhoopSleepId(String whoopSleepId) {
        this.whoopSleepId = whoopSleepId;
    }

    public LocalDate getRecoveryDate() {
        return recoveryDate;
    }

    public void setRecoveryDate(LocalDate recoveryDate) {
        this.recoveryDate = recoveryDate;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getScoreState() {
        return scoreState;
    }

    public void setScoreState(String scoreState) {
        this.scoreState = scoreState;
    }

    public Boolean getUserCalibrating() {
        return userCalibrating;
    }

    public void setUserCalibrating(Boolean userCalibrating) {
        this.userCalibrating = userCalibrating;
    }

    public Integer getRecoveryScore() {
        return recoveryScore;
    }

    public void setRecoveryScore(Integer recoveryScore) {
        this.recoveryScore = recoveryScore;
    }

    public BigDecimal getRestingHeartRate() {
        return restingHeartRate;
    }

    public void setRestingHeartRate(BigDecimal restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }

    public BigDecimal getHrvRmssdMilli() {
        return hrvRmssdMilli;
    }

    public void setHrvRmssdMilli(BigDecimal hrvRmssdMilli) {
        this.hrvRmssdMilli = hrvRmssdMilli;
    }

    public BigDecimal getSpo2Percentage() {
        return spo2Percentage;
    }

    public void setSpo2Percentage(BigDecimal spo2Percentage) {
        this.spo2Percentage = spo2Percentage;
    }

    public BigDecimal getSkinTempCelsius() {
        return skinTempCelsius;
    }

    public void setSkinTempCelsius(BigDecimal skinTempCelsius) {
        this.skinTempCelsius = skinTempCelsius;
    }

    public JsonNode getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(JsonNode rawPayload) {
        this.rawPayload = rawPayload;
    }
}
