package com.fittrack.whoop.service;

import com.fittrack.analytics.dto.WearableDaySnapshot;
import com.fittrack.analytics.service.EnergyCalculator;
import com.fittrack.analytics.service.WearableConnectionStatusPort;
import com.fittrack.analytics.service.WearableDataPort;
import com.fittrack.whoop.domain.WhoopConnectionRepository;
import com.fittrack.whoop.domain.WhoopCycle;
import com.fittrack.whoop.domain.WhoopCycleRepository;
import com.fittrack.whoop.domain.WhoopRecovery;
import com.fittrack.whoop.domain.WhoopRecoveryRepository;
import com.fittrack.whoop.domain.WhoopSleep;
import com.fittrack.whoop.domain.WhoopSleepRepository;
import com.fittrack.whoop.domain.WhoopWorkout;
import com.fittrack.whoop.domain.WhoopWorkoutRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Supplies WHOOP data to the analytics module through its own port.
 *
 * <p>This is the seam that keeps the dashboard and analytics free of any WHOOP concept: they know
 * only {@link WearableDaySnapshot}. Adding a second wearable means adding another adapter.
 */
@Service
public class WhoopWearableDataAdapter implements WearableDataPort, WearableConnectionStatusPort {

    private final WhoopCycleRepository cycleRepository;
    private final WhoopRecoveryRepository recoveryRepository;
    private final WhoopSleepRepository sleepRepository;
    private final WhoopWorkoutRepository workoutRepository;
    private final WhoopConnectionRepository connectionRepository;
    private final EnergyCalculator energyCalculator;

    public WhoopWearableDataAdapter(
            WhoopCycleRepository cycleRepository,
            WhoopRecoveryRepository recoveryRepository,
            WhoopSleepRepository sleepRepository,
            WhoopWorkoutRepository workoutRepository,
            WhoopConnectionRepository connectionRepository,
            EnergyCalculator energyCalculator) {
        this.cycleRepository = cycleRepository;
        this.recoveryRepository = recoveryRepository;
        this.sleepRepository = sleepRepository;
        this.workoutRepository = workoutRepository;
        this.connectionRepository = connectionRepository;
        this.energyCalculator = energyCalculator;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isConnected(UUID userId) {
        return connectionRepository.findByUserId(userId).filter(c -> c.isUsable()).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public WearableDaySnapshot forDay(UUID userId, LocalDate date, ZoneId zone) {
        WhoopCycle cycle =
                cycleRepository.findFirstByUserIdAndCycleDateOrderByStartAtDesc(userId, date).orElse(null);
        WhoopRecovery recovery = recoveryRepository
                .findFirstByUserIdAndRecoveryDateOrderByRecordedAtDesc(userId, date)
                .orElse(null);
        WhoopSleep sleep = sleepRepository
                .findFirstByUserIdAndSleepDateAndNapFalseOrderBySleepDurationMillisDesc(userId, date)
                .orElse(null);
        int workouts = workoutRepository
                .findByUserIdAndWorkoutDateBetweenOrderByStartAtAsc(userId, date, date)
                .size();

        return toSnapshot(date, cycle, recovery, sleep, workouts);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WearableDaySnapshot> forRange(
            UUID userId, LocalDate from, LocalDate to, ZoneId zone) {

        Map<LocalDate, WhoopCycle> cycles = new HashMap<>();
        for (WhoopCycle cycle :
                cycleRepository.findByUserIdAndCycleDateBetweenOrderByCycleDateAsc(userId, from, to)) {
            cycles.put(cycle.getCycleDate(), cycle);
        }

        Map<LocalDate, WhoopRecovery> recoveries = new HashMap<>();
        for (WhoopRecovery recovery :
                recoveryRepository.findByUserIdAndRecoveryDateBetweenOrderByRecoveryDateAsc(userId, from, to)) {
            recoveries.put(recovery.getRecoveryDate(), recovery);
        }

        Map<LocalDate, WhoopSleep> sleeps = new HashMap<>();
        for (WhoopSleep sleep :
                sleepRepository.findByUserIdAndSleepDateBetweenAndNapFalseOrderBySleepDateAsc(userId, from, to)) {
            // Keep the longest sleep when a day somehow has more than one main sleep.
            sleeps.merge(sleep.getSleepDate(), sleep, (existing, candidate) ->
                    millisOrZero(candidate) > millisOrZero(existing) ? candidate : existing);
        }

        Map<LocalDate, Integer> workoutCounts = new HashMap<>();
        for (WhoopWorkout workout :
                workoutRepository.findByUserIdAndWorkoutDateBetweenOrderByStartAtAsc(userId, from, to)) {
            workoutCounts.merge(workout.getWorkoutDate(), 1, Integer::sum);
        }

        List<WearableDaySnapshot> snapshots = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            snapshots.add(toSnapshot(
                    date,
                    cycles.get(date),
                    recoveries.get(date),
                    sleeps.get(date),
                    workoutCounts.getOrDefault(date, 0)));
        }
        return snapshots;
    }

    private WearableDaySnapshot toSnapshot(
            LocalDate date, WhoopCycle cycle, WhoopRecovery recovery, WhoopSleep sleep, int workouts) {
        return new WearableDaySnapshot(
                date,
                recovery == null ? null : recovery.getRecoveryScore(),
                recovery == null ? null : recovery.getRestingHeartRate(),
                recovery == null ? null : recovery.getHrvRmssdMilli(),
                cycle == null ? null : cycle.getStrain(),
                cycle == null ? null : energyCalculator.kilojoulesToKilocalories(cycle.getKilojoules()),
                sleep == null ? null : sleep.getSleepDurationMillis(),
                sleep == null ? null : sleep.getSleepNeedMillis(),
                sleep == null ? null : sleep.getSleepPerformancePercentage(),
                workouts);
    }

    private static long millisOrZero(WhoopSleep sleep) {
        return sleep.getSleepDurationMillis() == null ? 0 : sleep.getSleepDurationMillis();
    }
}
