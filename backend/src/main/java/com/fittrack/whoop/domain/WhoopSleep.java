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

/** A sleep activity. Identified by a UUID in v2, unique per user for idempotent upserts. */
@Entity
@Table(name = "whoop_sleeps")
public class WhoopSleep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "whoop_sleep_id", nullable = false, length = 64)
    private String whoopSleepId;

    @Column(name = "whoop_cycle_id")
    private Long whoopCycleId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    /** The date the sleep is attributed to: the calendar day it ended on. */
    @Column(name = "sleep_date", nullable = false)
    private LocalDate sleepDate;

    @Column(name = "timezone_offset", length = 16)
    private String timezoneOffset;

    @Column(name = "nap", nullable = false)
    private boolean nap;

    @Column(name = "score_state", length = 32)
    private String scoreState;

    @Column(name = "total_in_bed_time_millis")
    private Long totalInBedTimeMillis;

    @Column(name = "total_awake_time_millis")
    private Long totalAwakeTimeMillis;

    @Column(name = "total_light_sleep_time_millis")
    private Long totalLightSleepTimeMillis;

    @Column(name = "total_slow_wave_sleep_time_millis")
    private Long totalSlowWaveSleepTimeMillis;

    @Column(name = "total_rem_sleep_time_millis")
    private Long totalRemSleepTimeMillis;

    /** Time actually asleep: in bed minus awake. Precomputed because every view needs it. */
    @Column(name = "sleep_duration_millis")
    private Long sleepDurationMillis;

    @Column(name = "sleep_need_millis")
    private Long sleepNeedMillis;

    @Column(name = "disturbance_count")
    private Integer disturbanceCount;

    @Column(name = "sleep_cycle_count")
    private Integer sleepCycleCount;

    @Column(name = "respiratory_rate", precision = 6, scale = 3)
    private BigDecimal respiratoryRate;

    @Column(name = "sleep_performance_percentage")
    private Integer sleepPerformancePercentage;

    @Column(name = "sleep_consistency_percentage")
    private Integer sleepConsistencyPercentage;

    @Column(name = "sleep_efficiency_percentage", precision = 6, scale = 3)
    private BigDecimal sleepEfficiencyPercentage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload")
    private JsonNode rawPayload;

    protected WhoopSleep() {}

    public WhoopSleep(User user, String whoopSleepId) {
        this.user = user;
        this.whoopSleepId = whoopSleepId;
    }

    public User getUser() {
        return user;
    }

    public String getWhoopSleepId() {
        return whoopSleepId;
    }

    public Long getWhoopCycleId() {
        return whoopCycleId;
    }

    public void setWhoopCycleId(Long whoopCycleId) {
        this.whoopCycleId = whoopCycleId;
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

    public LocalDate getSleepDate() {
        return sleepDate;
    }

    public void setSleepDate(LocalDate sleepDate) {
        this.sleepDate = sleepDate;
    }

    public String getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(String timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public boolean isNap() {
        return nap;
    }

    public void setNap(boolean nap) {
        this.nap = nap;
    }

    public String getScoreState() {
        return scoreState;
    }

    public void setScoreState(String scoreState) {
        this.scoreState = scoreState;
    }

    public Long getTotalInBedTimeMillis() {
        return totalInBedTimeMillis;
    }

    public void setTotalInBedTimeMillis(Long totalInBedTimeMillis) {
        this.totalInBedTimeMillis = totalInBedTimeMillis;
    }

    public Long getTotalAwakeTimeMillis() {
        return totalAwakeTimeMillis;
    }

    public void setTotalAwakeTimeMillis(Long totalAwakeTimeMillis) {
        this.totalAwakeTimeMillis = totalAwakeTimeMillis;
    }

    public Long getTotalLightSleepTimeMillis() {
        return totalLightSleepTimeMillis;
    }

    public void setTotalLightSleepTimeMillis(Long totalLightSleepTimeMillis) {
        this.totalLightSleepTimeMillis = totalLightSleepTimeMillis;
    }

    public Long getTotalSlowWaveSleepTimeMillis() {
        return totalSlowWaveSleepTimeMillis;
    }

    public void setTotalSlowWaveSleepTimeMillis(Long totalSlowWaveSleepTimeMillis) {
        this.totalSlowWaveSleepTimeMillis = totalSlowWaveSleepTimeMillis;
    }

    public Long getTotalRemSleepTimeMillis() {
        return totalRemSleepTimeMillis;
    }

    public void setTotalRemSleepTimeMillis(Long totalRemSleepTimeMillis) {
        this.totalRemSleepTimeMillis = totalRemSleepTimeMillis;
    }

    public Long getSleepDurationMillis() {
        return sleepDurationMillis;
    }

    public void setSleepDurationMillis(Long sleepDurationMillis) {
        this.sleepDurationMillis = sleepDurationMillis;
    }

    public Long getSleepNeedMillis() {
        return sleepNeedMillis;
    }

    public void setSleepNeedMillis(Long sleepNeedMillis) {
        this.sleepNeedMillis = sleepNeedMillis;
    }

    public Integer getDisturbanceCount() {
        return disturbanceCount;
    }

    public void setDisturbanceCount(Integer disturbanceCount) {
        this.disturbanceCount = disturbanceCount;
    }

    public Integer getSleepCycleCount() {
        return sleepCycleCount;
    }

    public void setSleepCycleCount(Integer sleepCycleCount) {
        this.sleepCycleCount = sleepCycleCount;
    }

    public BigDecimal getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(BigDecimal respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Integer getSleepPerformancePercentage() {
        return sleepPerformancePercentage;
    }

    public void setSleepPerformancePercentage(Integer sleepPerformancePercentage) {
        this.sleepPerformancePercentage = sleepPerformancePercentage;
    }

    public Integer getSleepConsistencyPercentage() {
        return sleepConsistencyPercentage;
    }

    public void setSleepConsistencyPercentage(Integer sleepConsistencyPercentage) {
        this.sleepConsistencyPercentage = sleepConsistencyPercentage;
    }

    public BigDecimal getSleepEfficiencyPercentage() {
        return sleepEfficiencyPercentage;
    }

    public void setSleepEfficiencyPercentage(BigDecimal sleepEfficiencyPercentage) {
        this.sleepEfficiencyPercentage = sleepEfficiencyPercentage;
    }

    public JsonNode getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(JsonNode rawPayload) {
        this.rawPayload = rawPayload;
    }
}
