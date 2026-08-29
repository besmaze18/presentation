package com.fittrack.analytics.service;

import com.fittrack.user.domain.ActivityLevel;
import com.fittrack.user.domain.Sex;
import com.fittrack.user.domain.UserSettings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/**
 * Conventional, transparent energy calculations. Everything produced here is an <em>estimate</em>
 * and is labelled as such in the API; nothing in the app treats a wearable's reported burn as
 * ground-truth TDEE.
 *
 * <p>BMR uses Mifflin-St Jeor, which needs weight, height, age and sex. When any input is missing
 * the result is null rather than a fabricated number.
 */
@Component
public class EnergyCalculator {

    /** kcal per kilojoule. WHOOP reports expenditure in kilojoules. */
    private static final BigDecimal KCAL_PER_KILOJOULE = new BigDecimal("0.2390057");

    private static final BigDecimal MIFFLIN_WEIGHT_FACTOR = new BigDecimal("10");
    private static final BigDecimal MIFFLIN_HEIGHT_FACTOR = new BigDecimal("6.25");
    private static final BigDecimal MIFFLIN_AGE_FACTOR = new BigDecimal("5");
    private static final BigDecimal MIFFLIN_MALE_CONSTANT = new BigDecimal("5");
    private static final BigDecimal MIFFLIN_FEMALE_CONSTANT = new BigDecimal("-161");
    /** Midpoint of the male and female constants, used when sex is not stated. */
    private static final BigDecimal MIFFLIN_UNSPECIFIED_CONSTANT = new BigDecimal("-78");

    /**
     * Mifflin-St Jeor basal metabolic rate in kcal/day, or null when an input is unavailable.
     */
    public BigDecimal basalMetabolicRate(UserSettings settings, BigDecimal weightKg, LocalDate on) {
        if (settings == null || weightKg == null || settings.getHeightCm() == null || settings.getBirthDate() == null) {
            return null;
        }
        long age = ChronoUnit.YEARS.between(settings.getBirthDate(), on);
        if (age <= 0 || age > 120) {
            return null;
        }
        BigDecimal bmr = MIFFLIN_WEIGHT_FACTOR
                .multiply(weightKg)
                .add(MIFFLIN_HEIGHT_FACTOR.multiply(settings.getHeightCm()))
                .subtract(MIFFLIN_AGE_FACTOR.multiply(BigDecimal.valueOf(age)))
                .add(constantFor(settings.getSex()));
        return bmr.signum() <= 0 ? null : bmr.setScale(0, RoundingMode.HALF_UP);
    }

    /** BMR scaled by the user's stated activity level. Null when BMR could not be computed. */
    public BigDecimal totalDailyEnergyExpenditure(BigDecimal basalMetabolicRate, ActivityLevel level) {
        if (basalMetabolicRate == null) {
            return null;
        }
        ActivityLevel resolved = level == null ? ActivityLevel.MODERATE : level;
        return basalMetabolicRate.multiply(resolved.multiplier()).setScale(0, RoundingMode.HALF_UP);
    }

    public BigDecimal kilojoulesToKilocalories(BigDecimal kilojoules) {
        return kilojoules == null
                ? null
                : kilojoules.multiply(KCAL_PER_KILOJOULE).setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Intake minus expenditure. Positive means a surplus. The caller decides which expenditure
     * figure to pass, and the API states which basis was used.
     */
    public BigDecimal energyBalance(BigDecimal intakeKcal, BigDecimal expenditureKcal) {
        if (intakeKcal == null || expenditureKcal == null) {
            return null;
        }
        return intakeKcal.subtract(expenditureKcal).setScale(0, RoundingMode.HALF_UP);
    }

    private static BigDecimal constantFor(Sex sex) {
        if (sex == null) {
            return MIFFLIN_UNSPECIFIED_CONSTANT;
        }
        return switch (sex) {
            case MALE -> MIFFLIN_MALE_CONSTANT;
            case FEMALE -> MIFFLIN_FEMALE_CONSTANT;
            case UNSPECIFIED -> MIFFLIN_UNSPECIFIED_CONSTANT;
        };
    }
}
