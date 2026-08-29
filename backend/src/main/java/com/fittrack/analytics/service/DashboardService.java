package com.fittrack.analytics.service;

import com.fittrack.analytics.dto.DashboardResponse;
import com.fittrack.analytics.dto.EnergySummary;
import com.fittrack.analytics.dto.MacroProgress;
import com.fittrack.analytics.dto.WearableDaySnapshot;
import com.fittrack.common.util.Numbers;
import com.fittrack.nutrition.domain.DailyMacroTotals;
import com.fittrack.nutrition.service.NutritionService;
import com.fittrack.training.dto.TrainingSessionResponse;
import com.fittrack.training.service.TrainingService;
import com.fittrack.user.domain.NutritionGoal;
import com.fittrack.user.domain.UserSettings;
import com.fittrack.user.dto.WeightTrendResponse;
import com.fittrack.user.service.BodyMeasurementService;
import com.fittrack.user.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Assembles the Today screen. Everything here is computed deterministically from stored data. */
@Service
public class DashboardService {

    /** How far back the recovery baseline looks. */
    private static final int RECOVERY_BASELINE_DAYS = 30;

    private final NutritionService nutritionService;
    private final TrainingService trainingService;
    private final BodyMeasurementService bodyMeasurementService;
    private final UserService userService;
    private final WearableDataPort wearableDataPort;
    private final WearableConnectionStatusPort wearableConnectionStatusPort;
    private final EnergyCalculator energyCalculator;
    private final InsightCalculator insightCalculator;
    private final Clock clock;

    public DashboardService(
            NutritionService nutritionService,
            TrainingService trainingService,
            BodyMeasurementService bodyMeasurementService,
            UserService userService,
            WearableDataPort wearableDataPort,
            WearableConnectionStatusPort wearableConnectionStatusPort,
            EnergyCalculator energyCalculator,
            InsightCalculator insightCalculator,
            Clock clock) {
        this.nutritionService = nutritionService;
        this.trainingService = trainingService;
        this.bodyMeasurementService = bodyMeasurementService;
        this.userService = userService;
        this.wearableDataPort = wearableDataPort;
        this.wearableConnectionStatusPort = wearableConnectionStatusPort;
        this.energyCalculator = energyCalculator;
        this.insightCalculator = insightCalculator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse today(UUID userId, LocalDate requestedDate) {
        UserSettings settings = userService.requireSettings(userId);
        ZoneId zone = ZoneId.of(settings.getTimeZone());
        LocalDate date = requestedDate != null ? requestedDate : LocalDate.now(clock.withZone(zone));

        NutritionGoal goal = userService.goalInEffect(userId, date);
        DailyMacroTotals totals = nutritionService.totalsForDay(userId, date);

        DashboardResponse.NutritionSummary nutrition = new DashboardResponse.NutritionSummary(
                progress(totals.calories(), BigDecimal.valueOf(goal.getCalorieTarget())),
                progress(totals.proteinG(), goal.getProteinTargetG()),
                progress(totals.carbsG(), goal.getCarbsTargetG()),
                progress(totals.fatG(), goal.getFatTargetG()),
                progress(totals.fiberG(), goal.getFiberTargetG()),
                totals.entryCount());

        WeightTrendResponse weight =
                bodyMeasurementService.trend(userId, date, zone, goal.getTargetBodyWeightKg());
        BigDecimal weightKg = bodyMeasurementService.weightOn(userId, date, zone).orElse(null);

        WearableDaySnapshot wearable = wearableDataPort.forDay(userId, date, zone);
        List<TrainingSessionResponse> workouts = trainingService.forDay(userId, date);

        EnergySummary energy = buildEnergySummary(settings, totals, wearable, weightKg, date);
        Integer recoveryBaseline = recoveryBaseline(userId, date, zone);

        return new DashboardResponse(
                date,
                settings.getTimeZone(),
                nutrition,
                energy,
                weight,
                wearable,
                wearableConnectionStatusPort.isConnected(userId),
                workouts,
                insightCalculator.calculate(
                        nutrition, energy, weight, wearable, recoveryBaseline, workouts.size()));
    }

    private EnergySummary buildEnergySummary(
            UserSettings settings,
            DailyMacroTotals totals,
            WearableDaySnapshot wearable,
            BigDecimal weightKg,
            LocalDate date) {

        BigDecimal intake = Numbers.scale(Numbers.nullToZero(totals.calories()), 0);
        BigDecimal bmr = energyCalculator.basalMetabolicRate(settings, weightKg, date);
        BigDecimal tdee = energyCalculator.totalDailyEnergyExpenditure(bmr, settings.getActivityLevel());
        BigDecimal wearableExpenditure = wearable == null ? null : wearable.expenditureKcal();

        // A wearable measurement, when present, is the better basis - but it is still reported
        // separately from the conventional estimate rather than replacing it.
        BigDecimal basisValue;
        EnergySummary.BalanceBasis basis;
        if (wearableExpenditure != null) {
            basisValue = wearableExpenditure;
            basis = EnergySummary.BalanceBasis.WEARABLE_EXPENDITURE;
        } else if (tdee != null) {
            basisValue = tdee;
            basis = EnergySummary.BalanceBasis.ESTIMATED_TDEE;
        } else {
            basisValue = null;
            basis = EnergySummary.BalanceBasis.NONE;
        }

        return new EnergySummary(
                intake,
                wearableExpenditure,
                bmr,
                tdee,
                energyCalculator.energyBalance(intake, basisValue),
                basis,
                weightKg == null ? null : Numbers.oneDecimal(weightKg));
    }

    /** Mean recovery score over the trailing 30 days, used to say whether today is unusual. */
    private Integer recoveryBaseline(UUID userId, LocalDate date, ZoneId zone) {
        List<WearableDaySnapshot> history = wearableDataPort.forRange(
                userId, date.minusDays(RECOVERY_BASELINE_DAYS), date.minusDays(1), zone);
        OptionalDouble average = history.stream()
                .map(WearableDaySnapshot::recoveryScore)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        return average.isPresent() ? (int) Math.round(average.getAsDouble()) : null;
    }

    private static MacroProgress progress(BigDecimal consumedRaw, BigDecimal target) {
        BigDecimal consumed = Numbers.oneDecimal(Numbers.nullToZero(consumedRaw));
        if (target == null || target.signum() <= 0) {
            return new MacroProgress(consumed, target, null, null);
        }
        BigDecimal remaining = Numbers.oneDecimal(target.subtract(consumed));
        int percent = consumed
                .multiply(BigDecimal.valueOf(100))
                .divide(target, 0, RoundingMode.HALF_UP)
                .intValue();
        return new MacroProgress(consumed, Numbers.oneDecimal(target), remaining, percent);
    }
}
