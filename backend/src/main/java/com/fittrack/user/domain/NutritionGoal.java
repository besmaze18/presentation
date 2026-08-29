package com.fittrack.user.domain;

import com.fittrack.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A user's macro targets, versioned by {@code effectiveFrom}. Storing goals as a dated series
 * (rather than a single mutable row) is what allows targets to later become dynamic per day or
 * per training load without a schema rewrite: the resolver simply picks the row in effect.
 */
@Entity
@Table(name = "nutrition_goals")
public class NutritionGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "calorie_target", nullable = false)
    private Integer calorieTarget;

    @Column(name = "protein_target_g", nullable = false, precision = 6, scale = 1)
    private BigDecimal proteinTargetG;

    @Column(name = "carbs_target_g", nullable = false, precision = 6, scale = 1)
    private BigDecimal carbsTargetG;

    @Column(name = "fat_target_g", nullable = false, precision = 6, scale = 1)
    private BigDecimal fatTargetG;

    @Column(name = "fiber_target_g", nullable = false, precision = 6, scale = 1)
    private BigDecimal fiberTargetG;

    @Column(name = "target_body_weight_kg", precision = 5, scale = 2)
    private BigDecimal targetBodyWeightKg;

    protected NutritionGoal() {}

    public NutritionGoal(User user, LocalDate effectiveFrom) {
        this.user = user;
        this.effectiveFrom = effectiveFrom;
    }

    public User getUser() {
        return user;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Integer getCalorieTarget() {
        return calorieTarget;
    }

    public void setCalorieTarget(Integer calorieTarget) {
        this.calorieTarget = calorieTarget;
    }

    public BigDecimal getProteinTargetG() {
        return proteinTargetG;
    }

    public void setProteinTargetG(BigDecimal proteinTargetG) {
        this.proteinTargetG = proteinTargetG;
    }

    public BigDecimal getCarbsTargetG() {
        return carbsTargetG;
    }

    public void setCarbsTargetG(BigDecimal carbsTargetG) {
        this.carbsTargetG = carbsTargetG;
    }

    public BigDecimal getFatTargetG() {
        return fatTargetG;
    }

    public void setFatTargetG(BigDecimal fatTargetG) {
        this.fatTargetG = fatTargetG;
    }

    public BigDecimal getFiberTargetG() {
        return fiberTargetG;
    }

    public void setFiberTargetG(BigDecimal fiberTargetG) {
        this.fiberTargetG = fiberTargetG;
    }

    public BigDecimal getTargetBodyWeightKg() {
        return targetBodyWeightKg;
    }

    public void setTargetBodyWeightKg(BigDecimal targetBodyWeightKg) {
        this.targetBodyWeightKg = targetBodyWeightKg;
    }
}
