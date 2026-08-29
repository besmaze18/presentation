package com.fittrack.analytics.service;

import com.fittrack.analytics.dto.AnalyticsSeriesResponse;
import com.fittrack.analytics.dto.WearableDaySnapshot;
import com.fittrack.common.util.DateRanges;
import com.fittrack.common.util.Numbers;
import com.fittrack.nutrition.domain.DailyMacroTotals;
import com.fittrack.nutrition.service.NutritionService;
import com.fittrack.training.domain.DailyTrainingTotals;
import com.fittrack.training.service.TrainingService;
import com.fittrack.user.domain.BodyMeasurement;
import com.fittrack.user.service.BodyMeasurementService;
import com.fittrack.user.service.UserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Day, week and month history. Aggregation happens in the database and the bucketing happens here,
 * so the browser receives a compact series rather than the whole history.
 */
@Service
public class AnalyticsService {

    public enum Granularity {
        DAY,
        WEEK,
        MONTH
    }

    private final NutritionService nutritionService;
    private final TrainingService trainingService;
    private final BodyMeasurementService bodyMeasurementService;
    private final UserService userService;
    private final WearableDataPort wearableDataPort;

    public AnalyticsService(
            NutritionService nutritionService,
            TrainingService trainingService,
            BodyMeasurementService bodyMeasurementService,
            UserService userService,
            WearableDataPort wearableDataPort) {
        this.nutritionService = nutritionService;
        this.trainingService = trainingService;
        this.bodyMeasurementService = bodyMeasurementService;
        this.userService = userService;
        this.wearableDataPort = wearableDataPort;
    }

    @Transactional(readOnly = true)
    public AnalyticsSeriesResponse series(
            UUID userId, LocalDate from, LocalDate to, Granularity granularity) {
        DateRanges.validateRange(from, to);
        ZoneId zone = userService.zoneOf(userId);

        Map<LocalDate, DailyMacroTotals> nutritionByDay = index(
                nutritionService.aggregateByDay(userId, from, to), DailyMacroTotals::date);
        Map<LocalDate, DailyTrainingTotals> trainingByDay = index(
                trainingService.aggregateByDay(userId, from, to), DailyTrainingTotals::date);
        Map<LocalDate, WearableDaySnapshot> wearableByDay = index(
                wearableDataPort.forRange(userId, from, to, zone), WearableDaySnapshot::date);
        Map<LocalDate, BigDecimal> weightByDay = weightByDay(userId, from, to, zone);

        List<DayRow> days = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            days.add(new DayRow(
                    date,
                    nutritionByDay.get(date),
                    trainingByDay.get(date),
                    wearableByDay.get(date),
                    weightByDay.get(date)));
        }

        List<AnalyticsSeriesResponse.Bucket> buckets = bucket(days, granularity);
        return new AnalyticsSeriesResponse(
                from, to, granularity.name(), buckets, totals(days, weightByDay));
    }

    /**
     * Weight carried forward: a day with no reading inherits the last known weight, so the chart
     * shows a continuous trend rather than gaps on days the user did not step on the scale.
     */
    private Map<LocalDate, BigDecimal> weightByDay(
            UUID userId, LocalDate from, LocalDate to, ZoneId zone) {
        Map<LocalDate, List<BigDecimal>> readings = new TreeMap<>();
        List<BodyMeasurement> measurements = bodyMeasurementService.between(
                userId,
                DateRanges.startOfDay(from, zone),
                DateRanges.endOfDayExclusive(to, zone));
        for (BodyMeasurement measurement : measurements) {
            readings
                    .computeIfAbsent(LocalDate.ofInstant(measurement.getRecordedAt(), zone), key -> new ArrayList<>())
                    .add(measurement.getWeightKg());
        }

        Map<LocalDate, BigDecimal> result = new LinkedHashMap<>();
        BigDecimal carried = bodyMeasurementService.weightOn(userId, from.minusDays(1), zone).orElse(null);
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<BigDecimal> forDay = readings.get(date);
            if (forDay != null && !forDay.isEmpty()) {
                BigDecimal sum = forDay.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                carried = sum.divide(BigDecimal.valueOf(forDay.size()), 1, RoundingMode.HALF_UP);
            }
            if (carried != null) {
                result.put(date, carried);
            }
        }
        return result;
    }

    private List<AnalyticsSeriesResponse.Bucket> bucket(List<DayRow> days, Granularity granularity) {
        Map<LocalDate, List<DayRow>> grouped = new LinkedHashMap<>();
        for (DayRow day : days) {
            grouped.computeIfAbsent(bucketStart(day.date(), granularity), key -> new ArrayList<>()).add(day);
        }
        return grouped.entrySet().stream()
                .map(entry -> toBucket(entry.getKey(), entry.getValue(), granularity))
                .toList();
    }

    private static LocalDate bucketStart(LocalDate date, Granularity granularity) {
        return switch (granularity) {
            case DAY -> date;
            case WEEK -> date.with(WeekFields.ISO.dayOfWeek(), 1);
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    private AnalyticsSeriesResponse.Bucket toBucket(
            LocalDate start, List<DayRow> rows, Granularity granularity) {

        // Macros are averaged per day within a bucket - a weekly total would not be comparable
        // to a daily target, whereas a daily average is.
        BigDecimal calories = averageOf(rows, row -> row.nutrition() == null ? null : row.nutrition().calories());
        BigDecimal protein = averageOf(rows, row -> row.nutrition() == null ? null : row.nutrition().proteinG());
        BigDecimal carbs = averageOf(rows, row -> row.nutrition() == null ? null : row.nutrition().carbsG());
        BigDecimal fat = averageOf(rows, row -> row.nutrition() == null ? null : row.nutrition().fatG());
        BigDecimal fiber = averageOf(rows, row -> row.nutrition() == null ? null : row.nutrition().fiberG());

        long foodEntries = rows.stream()
                .mapToLong(row -> row.nutrition() == null ? 0 : row.nutrition().entryCount())
                .sum();
        long workouts = rows.stream()
                .mapToLong(row -> row.training() == null ? 0 : row.training().sessionCount())
                .sum();
        long minutes = rows.stream()
                .mapToLong(row -> row.training() == null ? 0 : row.training().totalDurationMinutes())
                .sum();

        BigDecimal weight = rows.stream()
                .map(DayRow::weightKg)
                .filter(java.util.Objects::nonNull)
                .reduce((first, second) -> second)
                .orElse(null);

        Integer recovery = averageInt(rows, row -> row.wearable() == null ? null : row.wearable().recoveryScore());
        BigDecimal strain = averageOf(rows, row -> row.wearable() == null ? null : row.wearable().strain());
        Long sleep = averageLong(rows, row -> row.wearable() == null ? null : row.wearable().sleepDurationMillis());

        return new AnalyticsSeriesResponse.Bucket(
                start,
                label(start, granularity),
                calories,
                protein,
                carbs,
                fat,
                fiber,
                foodEntries,
                weight,
                workouts,
                minutes,
                recovery,
                strain,
                sleep);
    }

    private static String label(LocalDate start, Granularity granularity) {
        return switch (granularity) {
            case DAY -> start.toString();
            case WEEK -> "W" + start.get(WeekFields.ISO.weekOfWeekBasedYear());
            case MONTH -> start.getMonth().getDisplayName(
                            java.time.format.TextStyle.SHORT, Locale.ENGLISH)
                    + " " + start.getYear();
        };
    }

    private AnalyticsSeriesResponse.Totals totals(
            List<DayRow> days, Map<LocalDate, BigDecimal> weightByDay) {

        List<DayRow> loggedDays = days.stream()
                .filter(row -> row.nutrition() != null && row.nutrition().entryCount() > 0)
                .toList();

        BigDecimal weightChange = null;
        if (weightByDay.size() >= 2) {
            List<BigDecimal> values = List.copyOf(weightByDay.values());
            weightChange = Numbers.oneDecimal(
                    values.get(values.size() - 1).subtract(values.get(0)));
        }

        BigDecimal averageSleepHours = null;
        Long averageSleepMillis = averageLong(
                days, row -> row.wearable() == null ? null : row.wearable().sleepDurationMillis());
        if (averageSleepMillis != null) {
            averageSleepHours = BigDecimal.valueOf(averageSleepMillis)
                    .divide(BigDecimal.valueOf(3_600_000L), 1, RoundingMode.HALF_UP);
        }

        return new AnalyticsSeriesResponse.Totals(
                averageOf(loggedDays, row -> row.nutrition().calories()),
                averageOf(loggedDays, row -> row.nutrition().proteinG()),
                averageOf(loggedDays, row -> row.nutrition().carbsG()),
                averageOf(loggedDays, row -> row.nutrition().fatG()),
                averageOf(loggedDays, row -> row.nutrition().fiberG()),
                days.stream().mapToLong(row -> row.training() == null ? 0 : row.training().sessionCount()).sum(),
                days.stream()
                        .mapToLong(row -> row.training() == null ? 0 : row.training().totalDurationMinutes())
                        .sum(),
                averageOfInt(days, row -> row.wearable() == null ? null : row.wearable().recoveryScore()),
                averageOf(days, row -> row.wearable() == null ? null : row.wearable().strain()),
                averageSleepHours,
                weightChange,
                loggedDays.size());
    }

    private static <T> Map<LocalDate, T> index(
            List<T> items, java.util.function.Function<T, LocalDate> keyExtractor) {
        Map<LocalDate, T> map = new HashMap<>();
        for (T item : items) {
            map.put(keyExtractor.apply(item), item);
        }
        return map;
    }

    private static BigDecimal averageOf(
            List<DayRow> rows, java.util.function.Function<DayRow, BigDecimal> extractor) {
        List<BigDecimal> values = rows.stream().map(extractor).filter(java.util.Objects::nonNull).toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal averageOfInt(
            List<DayRow> rows, java.util.function.Function<DayRow, Integer> extractor) {
        Integer average = averageInt(rows, extractor);
        return average == null ? null : BigDecimal.valueOf(average);
    }

    private static Integer averageInt(
            List<DayRow> rows, java.util.function.Function<DayRow, Integer> extractor) {
        var values = rows.stream().map(extractor).filter(java.util.Objects::nonNull).toList();
        if (values.isEmpty()) {
            return null;
        }
        return (int) Math.round(values.stream().mapToInt(Integer::intValue).average().orElse(0));
    }

    private static Long averageLong(
            List<DayRow> rows, java.util.function.Function<DayRow, Long> extractor) {
        var values = rows.stream().map(extractor).filter(java.util.Objects::nonNull).toList();
        if (values.isEmpty()) {
            return null;
        }
        return Math.round(values.stream().mapToLong(Long::longValue).average().orElse(0));
    }

    /** Chooses a sensible bucket size for a range if the caller does not specify one. */
    public static Granularity defaultGranularityFor(LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to);
        if (days <= 31) {
            return Granularity.DAY;
        }
        return days <= 180 ? Granularity.WEEK : Granularity.MONTH;
    }

    private record DayRow(
            LocalDate date,
            DailyMacroTotals nutrition,
            DailyTrainingTotals training,
            WearableDaySnapshot wearable,
            BigDecimal weightKg) {}
}
