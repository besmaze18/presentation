package com.fittrack.nutrition.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The five tracked nutrients. Embedded rather than repeated so entries, items and saved foods
 * all share exactly one definition of what a macro breakdown is.
 */
@Embeddable
public class Macros {

    @Column(name = "calories", nullable = false, precision = 8, scale = 2)
    private BigDecimal calories = BigDecimal.ZERO.setScale(2);

    @Column(name = "protein_g", nullable = false, precision = 7, scale = 2)
    private BigDecimal proteinG = BigDecimal.ZERO.setScale(2);

    @Column(name = "carbs_g", nullable = false, precision = 7, scale = 2)
    private BigDecimal carbsG = BigDecimal.ZERO.setScale(2);

    @Column(name = "fat_g", nullable = false, precision = 7, scale = 2)
    private BigDecimal fatG = BigDecimal.ZERO.setScale(2);

    @Column(name = "fiber_g", nullable = false, precision = 7, scale = 2)
    private BigDecimal fiberG = BigDecimal.ZERO.setScale(2);

    /** Every nutrient is normalised to two decimals so API output has a stable shape. */
    private static final int SCALE = 2;

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);

    public Macros() {}

    public Macros(
            BigDecimal calories,
            BigDecimal proteinG,
            BigDecimal carbsG,
            BigDecimal fatG,
            BigDecimal fiberG) {
        this.calories = orZero(calories);
        this.proteinG = orZero(proteinG);
        this.carbsG = orZero(carbsG);
        this.fatG = orZero(fatG);
        this.fiberG = orZero(fiberG);
    }

    public static Macros zero() {
        return new Macros();
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? ZERO : value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public Macros plus(Macros other) {
        if (other == null) {
            return this;
        }
        return new Macros(
                calories.add(other.calories),
                proteinG.add(other.proteinG),
                carbsG.add(other.carbsG),
                fatG.add(other.fatG),
                fiberG.add(other.fiberG));
    }

    /** Scales every nutrient by a factor - used when a saved food is logged at a new quantity. */
    public Macros scaled(BigDecimal factor) {
        if (factor == null) {
            return this;
        }
        return new Macros(
                calories.multiply(factor),
                proteinG.multiply(factor),
                carbsG.multiply(factor),
                fatG.multiply(factor),
                fiberG.multiply(factor));
    }

    public BigDecimal getCalories() {
        return calories;
    }

    public void setCalories(BigDecimal calories) {
        this.calories = orZero(calories);
    }

    public BigDecimal getProteinG() {
        return proteinG;
    }

    public void setProteinG(BigDecimal proteinG) {
        this.proteinG = orZero(proteinG);
    }

    public BigDecimal getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(BigDecimal carbsG) {
        this.carbsG = orZero(carbsG);
    }

    public BigDecimal getFatG() {
        return fatG;
    }

    public void setFatG(BigDecimal fatG) {
        this.fatG = orZero(fatG);
    }

    public BigDecimal getFiberG() {
        return fiberG;
    }

    public void setFiberG(BigDecimal fiberG) {
        this.fiberG = orZero(fiberG);
    }
}
