package com.fittrack.analytics.service;

import com.fittrack.analytics.dto.DashboardResponse;
import com.fittrack.analytics.dto.EnergySummary;
import com.fittrack.analytics.dto.InsightDto;
import com.fittrack.analytics.dto.MacroProgress;
import com.fittrack.analytics.dto.WearableDaySnapshot;
import com.fittrack.common.util.Numbers;
import com.fittrack.user.dto.WeightTrendResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Produces the dashboard's short observations.
 *
 * <p>Every sentence here is computed from stored numbers - no language model is involved in
 * deriving them. An AI layer may later <em>explain</em> these conversationally, but it must never
 * be the source of the figures.
 */
@Component
public class InsightCalculator {

    /** Below this many kcal remaining, "nearly there" is more useful than "x remaining". */
    private static final BigDecimal CALORIES_CLOSE_ENOUGH = new BigDecimal("100");

    /** A weight-average move smaller than this is noise, not a trend. */
    private static final BigDecimal WEIGHT_TREND_THRESHOLD = new BigDecimal("0.2");

    /** Recovery must differ from the baseline by this many points to be worth remarking on. */
    private static final int RECOVERY_DEVIATION_POINTS = 8;

    private static final int HIGH_STRAIN = 14;

    public List<InsightDto> calculate(
            DashboardResponse.NutritionSummary nutrition,
            EnergySummary energy,
            WeightTrendResponse weight,
            WearableDaySnapshot wearable,
            Integer recoveryBaseline,
            int workoutsToday) {

        List<InsightDto> insights = new ArrayList<>();

        addCalorieInsight(insights, nutrition.calories());
        addProteinInsight(insights, nutrition.protein());
        addFiberInsight(insights, nutrition.fiber());
        addWeightInsight(insights, weight);
        addRecoveryInsight(insights, wearable, recoveryBaseline);
        addSleepInsight(insights, wearable);
        addStrainInsight(insights, wearable, workoutsToday);
        addEnergyBalanceInsight(insights, energy);

        return insights;
    }

    private void addCalorieInsight(List<InsightDto> insights, MacroProgress calories) {
        if (calories.target() == null || calories.target().signum() <= 0) {
            return;
        }
        BigDecimal remaining = calories.remaining();
        if (remaining == null) {
            return;
        }
        if (remaining.signum() < 0) {
            insights.add(InsightDto.warning(
                    "CALORIES_OVER",
                    Numbers.roundToInt(remaining.abs()) + " kcal over your target"));
        } else if (remaining.compareTo(CALORIES_CLOSE_ENOUGH) <= 0) {
            insights.add(InsightDto.positive("CALORIES_ON_TARGET", "Calories are on target for today"));
        } else {
            insights.add(InsightDto.neutral(
                    "CALORIES_REMAINING", Numbers.roundToInt(remaining) + " kcal remaining"));
        }
    }

    private void addProteinInsight(List<InsightDto> insights, MacroProgress protein) {
        if (protein.target() == null || protein.target().signum() <= 0 || protein.remaining() == null) {
            return;
        }
        if (protein.remaining().signum() > 0) {
            insights.add(InsightDto.neutral(
                    "PROTEIN_REMAINING",
                    Numbers.roundToInt(protein.remaining()) + " g protein remaining"));
        } else {
            insights.add(InsightDto.positive("PROTEIN_MET", "Protein target met"));
        }
    }

    private void addFiberInsight(List<InsightDto> insights, MacroProgress fiber) {
        if (fiber.target() == null || fiber.target().signum() <= 0 || fiber.percentOfTarget() == null) {
            return;
        }
        if (fiber.percentOfTarget() < 50) {
            insights.add(InsightDto.neutral(
                    "FIBER_LOW",
                    "Fiber is at " + fiber.percentOfTarget() + "% of target"));
        }
    }

    private void addWeightInsight(List<InsightDto> insights, WeightTrendResponse weight) {
        if (weight == null || weight.change7dKg() == null) {
            return;
        }
        BigDecimal change = weight.change7dKg();
        if (change.abs().compareTo(WEIGHT_TREND_THRESHOLD) < 0) {
            insights.add(InsightDto.neutral("WEIGHT_STABLE", "7-day weight average is stable"));
            return;
        }
        String direction = change.signum() < 0 ? "decreased" : "increased";
        insights.add(InsightDto.neutral(
                change.signum() < 0 ? "WEIGHT_DOWN" : "WEIGHT_UP",
                "7-day weight average " + direction + " " + change.abs().toPlainString() + " kg"));
    }

    private void addRecoveryInsight(
            List<InsightDto> insights, WearableDaySnapshot wearable, Integer baseline) {
        if (wearable == null || wearable.recoveryScore() == null) {
            return;
        }
        int recovery = wearable.recoveryScore();
        if (baseline == null) {
            insights.add(InsightDto.neutral("RECOVERY_TODAY", "Recovery is " + recovery + "%"));
            return;
        }
        int delta = recovery - baseline;
        if (Math.abs(delta) < RECOVERY_DEVIATION_POINTS) {
            insights.add(InsightDto.neutral(
                    "RECOVERY_TYPICAL", "Recovery is in line with your 30-day average"));
        } else if (delta > 0) {
            insights.add(InsightDto.positive(
                    "RECOVERY_ABOVE_BASELINE",
                    "Recovery is " + delta + " points above your 30-day average"));
        } else {
            insights.add(InsightDto.warning(
                    "RECOVERY_BELOW_BASELINE",
                    "Recovery is " + Math.abs(delta) + " points below your 30-day average"));
        }
    }

    private void addSleepInsight(List<InsightDto> insights, WearableDaySnapshot wearable) {
        if (wearable == null || wearable.sleepDurationMillis() == null) {
            return;
        }
        long minutes = wearable.sleepDurationMillis() / 60_000;
        String formatted = (minutes / 60) + "h " + (minutes % 60) + "m";
        if (wearable.sleepNeedMillis() != null && wearable.sleepNeedMillis() > 0) {
            long deficitMinutes = (wearable.sleepNeedMillis() - wearable.sleepDurationMillis()) / 60_000;
            if (deficitMinutes > 30) {
                insights.add(InsightDto.warning(
                        "SLEEP_DEFICIT",
                        "Slept " + formatted + ", " + deficitMinutes + " min short of your need"));
                return;
            }
            insights.add(InsightDto.positive("SLEEP_MET", "Slept " + formatted + ", meeting your need"));
            return;
        }
        insights.add(InsightDto.neutral("SLEEP_DURATION", "Slept " + formatted));
    }

    private void addStrainInsight(
            List<InsightDto> insights, WearableDaySnapshot wearable, int workoutsToday) {
        if (wearable != null && wearable.strain() != null
                && wearable.strain().compareTo(BigDecimal.valueOf(HIGH_STRAIN)) >= 0) {
            insights.add(InsightDto.neutral(
                    "STRAIN_HIGH", "Strain is " + Numbers.scale(wearable.strain(), 1).toPlainString()));
            return;
        }
        if (workoutsToday > 0) {
            insights.add(InsightDto.neutral(
                    "WORKOUTS_TODAY",
                    workoutsToday + (workoutsToday == 1 ? " workout logged today" : " workouts logged today")));
        }
    }

    private void addEnergyBalanceInsight(List<InsightDto> insights, EnergySummary energy) {
        if (energy.balanceKcal() == null
                || energy.balanceBasis() == EnergySummary.BalanceBasis.NONE) {
            return;
        }
        String basis = energy.balanceBasis() == EnergySummary.BalanceBasis.WEARABLE_EXPENDITURE
                ? "wearable expenditure"
                : "estimated TDEE";
        int balance = Numbers.roundToInt(energy.balanceKcal());
        String phrase = balance >= 0
                ? balance + " kcal surplus vs " + basis
                : Math.abs(balance) + " kcal deficit vs " + basis;
        insights.add(InsightDto.neutral("ENERGY_BALANCE", phrase + " (estimate)"));
    }
}
