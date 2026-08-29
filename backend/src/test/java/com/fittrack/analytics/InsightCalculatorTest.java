package com.fittrack.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import com.fittrack.analytics.dto.DashboardResponse;
import com.fittrack.analytics.dto.EnergySummary;
import com.fittrack.analytics.dto.InsightDto;
import com.fittrack.analytics.dto.MacroProgress;
import com.fittrack.analytics.dto.WearableDaySnapshot;
import com.fittrack.analytics.service.InsightCalculator;
import com.fittrack.user.dto.WeightTrendResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Insights are the one place where a wrong number would be most visible, so each rule is pinned
 * to an exact sentence.
 */
class InsightCalculatorTest {

    private static final LocalDate DAY = LocalDate.of(2026, 3, 10);

    private final InsightCalculator calculator = new InsightCalculator();

    private static MacroProgress progress(String consumed, String target) {
        BigDecimal c = new BigDecimal(consumed);
        BigDecimal t = new BigDecimal(target);
        BigDecimal remaining = t.subtract(c);
        int percent = c.multiply(BigDecimal.valueOf(100))
                .divide(t, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
        return new MacroProgress(c, t, remaining, percent);
    }

    private static DashboardResponse.NutritionSummary nutrition(
            MacroProgress calories, MacroProgress protein, MacroProgress fiber) {
        MacroProgress neutral = progress("100", "200");
        return new DashboardResponse.NutritionSummary(calories, protein, neutral, neutral, fiber, 3);
    }

    private static List<String> textsOf(List<InsightDto> insights) {
        return insights.stream().map(InsightDto::text).toList();
    }

    private static InsightDto find(List<InsightDto> insights, String code) {
        return insights.stream()
                .filter(insight -> insight.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No insight with code " + code + " in " + textsOf(insights)));
    }

    @Test
    void reportsRemainingCaloriesAndProtein() {
        List<InsightDto> insights = calculator.calculate(
                nutrition(progress("1980", "2400"), progress("125", "160"), progress("20", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                null,
                null,
                null,
                0);

        assertThat(find(insights, "CALORIES_REMAINING").text()).isEqualTo("420 kcal remaining");
        assertThat(find(insights, "PROTEIN_REMAINING").text()).isEqualTo("35 g protein remaining");
    }

    @Test
    void flagsGoingOverTheCalorieTarget() {
        List<InsightDto> insights = calculator.calculate(
                nutrition(progress("2650", "2400"), progress("170", "160"), progress("30", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                null,
                null,
                null,
                0);

        InsightDto over = find(insights, "CALORIES_OVER");
        assertThat(over.text()).isEqualTo("250 kcal over your target");
        assertThat(over.tone()).isEqualTo(InsightDto.Tone.WARNING);
        assertThat(find(insights, "PROTEIN_MET").tone()).isEqualTo(InsightDto.Tone.POSITIVE);
    }

    @Test
    void describesTheSevenDayWeightTrendOnlyWhenItExceedsNoise() {
        WeightTrendResponse moving = new WeightTrendResponse(
                new BigDecimal("82.0"), null, new BigDecimal("82.1"), null,
                new BigDecimal("-0.4"), null, null, null);
        List<InsightDto> insights = calculator.calculate(
                nutrition(progress("2000", "2400"), progress("150", "160"), progress("30", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                moving,
                null,
                null,
                0);
        assertThat(find(insights, "WEIGHT_DOWN").text())
                .isEqualTo("7-day weight average decreased 0.4 kg");

        WeightTrendResponse stable = new WeightTrendResponse(
                new BigDecimal("82.0"), null, new BigDecimal("82.0"), null,
                new BigDecimal("-0.1"), null, null, null);
        List<InsightDto> stableInsights = calculator.calculate(
                nutrition(progress("2000", "2400"), progress("150", "160"), progress("30", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                stable,
                null,
                null,
                0);
        assertThat(find(stableInsights, "WEIGHT_STABLE").text())
                .isEqualTo("7-day weight average is stable");
    }

    @Test
    void comparesRecoveryAgainstTheThirtyDayBaseline() {
        WearableDaySnapshot wearable = new WearableDaySnapshot(
                DAY, 74, null, null, null, null, null, null, null, 0);

        List<InsightDto> above = calculator.calculate(
                nutrition(progress("2000", "2400"), progress("150", "160"), progress("30", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                null,
                wearable,
                60,
                0);
        InsightDto insight = find(above, "RECOVERY_ABOVE_BASELINE");
        assertThat(insight.text()).isEqualTo("Recovery is 14 points above your 30-day average");
        assertThat(insight.tone()).isEqualTo(InsightDto.Tone.POSITIVE);

        List<InsightDto> typical = calculator.calculate(
                nutrition(progress("2000", "2400"), progress("150", "160"), progress("30", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                null,
                wearable,
                71,
                0);
        assertThat(find(typical, "RECOVERY_TYPICAL")).isNotNull();
    }

    @Test
    void reportsASleepDeficitAgainstTheReportedNeed() {
        WearableDaySnapshot wearable = new WearableDaySnapshot(
                DAY, null, null, null, null, null,
                6L * 3_600_000L, // slept 6h
                7L * 3_600_000L + 1_800_000L, // needed 7h30
                null,
                0);

        List<InsightDto> insights = calculator.calculate(
                nutrition(progress("2000", "2400"), progress("150", "160"), progress("30", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                null,
                wearable,
                null,
                0);

        InsightDto sleep = find(insights, "SLEEP_DEFICIT");
        assertThat(sleep.text()).isEqualTo("Slept 6h 0m, 90 min short of your need");
        assertThat(sleep.tone()).isEqualTo(InsightDto.Tone.WARNING);
    }

    @Test
    void namesWhichExpenditureFigureTheBalanceUsedAndMarksItAnEstimate() {
        List<InsightDto> insights = calculator.calculate(
                nutrition(progress("2000", "2400"), progress("150", "160"), progress("30", "30")),
                new EnergySummary(
                        new BigDecimal("2000"),
                        null,
                        new BigDecimal("1780"),
                        new BigDecimal("2759"),
                        new BigDecimal("-759"),
                        EnergySummary.BalanceBasis.ESTIMATED_TDEE,
                        new BigDecimal("80.0")),
                null,
                null,
                null,
                0);

        assertThat(find(insights, "ENERGY_BALANCE").text())
                .isEqualTo("759 kcal deficit vs estimated TDEE (estimate)");
    }

    @Test
    void producesNoWearableInsightsWhenThereIsNoWearableData() {
        List<InsightDto> insights = calculator.calculate(
                nutrition(progress("2000", "2400"), progress("150", "160"), progress("30", "30")),
                new EnergySummary(null, null, null, null, null, EnergySummary.BalanceBasis.NONE, null),
                null,
                WearableDaySnapshot.empty(DAY),
                null,
                0);

        assertThat(insights).extracting(InsightDto::code)
                .doesNotContain("RECOVERY_TODAY", "SLEEP_DURATION", "STRAIN_HIGH");
    }
}
