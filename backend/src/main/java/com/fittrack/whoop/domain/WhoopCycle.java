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
 * A WHOOP physiological cycle - roughly a day, bounded by sleep. Carries day strain and total
 * energy expenditure.
 *
 * <p>{@code whoopCycleId} is unique per user, which is what makes repeated synchronisation
 * idempotent. The untouched payload is kept in JSONB so a field WHOOP adds later is not lost
 * before the schema catches up.
 */
@Entity
@Table(name = "whoop_cycles")
public class WhoopCycle extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "whoop_cycle_id", nullable = false)
    private Long whoopCycleId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "cycle_date", nullable = false)
    private LocalDate cycleDate;

    @Column(name = "timezone_offset", length = 16)
    private String timezoneOffset;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload")
    private JsonNode rawPayload;

    protected WhoopCycle() {}

    public WhoopCycle(User user, Long whoopCycleId) {
        this.user = user;
        this.whoopCycleId = whoopCycleId;
    }

    public User getUser() {
        return user;
    }

    public Long getWhoopCycleId() {
        return whoopCycleId;
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

    public LocalDate getCycleDate() {
        return cycleDate;
    }

    public void setCycleDate(LocalDate cycleDate) {
        this.cycleDate = cycleDate;
    }

    public String getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(String timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
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

    public JsonNode getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(JsonNode rawPayload) {
        this.rawPayload = rawPayload;
    }
}
