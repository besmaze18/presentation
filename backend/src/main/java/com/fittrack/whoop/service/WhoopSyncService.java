package com.fittrack.whoop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fittrack.analytics.service.EnergyCalculator;
import com.fittrack.training.domain.TrainingCategory;
import com.fittrack.training.domain.TrainingSession;
import com.fittrack.training.domain.TrainingSessionRepository;
import com.fittrack.training.domain.TrainingSource;
import com.fittrack.user.domain.BodyMeasurement;
import com.fittrack.user.domain.BodyMeasurementRepository;
import com.fittrack.user.domain.MeasurementSource;
import com.fittrack.user.domain.User;
import com.fittrack.user.service.UserService;
import com.fittrack.whoop.client.WhoopApiException;
import com.fittrack.whoop.client.WhoopClient;
import com.fittrack.whoop.config.WhoopProperties;
import com.fittrack.whoop.domain.WhoopConnection;
import com.fittrack.whoop.domain.WhoopConnectionRepository;
import com.fittrack.whoop.domain.WhoopCycle;
import com.fittrack.whoop.domain.WhoopCycleRepository;
import com.fittrack.whoop.domain.WhoopRecovery;
import com.fittrack.whoop.domain.WhoopRecoveryRepository;
import com.fittrack.whoop.domain.WhoopSleep;
import com.fittrack.whoop.domain.WhoopSleepRepository;
import com.fittrack.whoop.domain.WhoopWorkout;
import com.fittrack.whoop.domain.WhoopWorkoutRepository;
import com.fittrack.whoop.dto.WhoopSyncResult;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pulls WHOOP data into local storage.
 *
 * <p>Every record is upserted on its WHOOP identifier, which is unique per user, so running a sync
 * twice over the same window updates rows rather than duplicating them. The first run reaches back
 * {@code initialSyncDays}; later runs start from the previous window's end minus a small overlap,
 * because WHOOP finalises a night's sleep and recovery some time after the activity ends.
 *
 * <p>Workouts are additionally projected into {@link TrainingSession} rows so imported and manual
 * training appear in one list. That projection is keyed on the WHOOP id too, and it deliberately
 * preserves any title, category, exertion rating or notes the user has since added.
 */
@Service
public class WhoopSyncService {

    private static final Logger log = LoggerFactory.getLogger(WhoopSyncService.class);

    private final WhoopProperties properties;
    private final WhoopClient whoopClient;
    private final WhoopOAuthService oAuthService;
    private final WhoopConnectionStateRecorder stateRecorder;
    private final WhoopConnectionRepository connectionRepository;
    private final WhoopCycleRepository cycleRepository;
    private final WhoopRecoveryRepository recoveryRepository;
    private final WhoopSleepRepository sleepRepository;
    private final WhoopWorkoutRepository workoutRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final BodyMeasurementRepository bodyMeasurementRepository;
    private final UserService userService;
    private final EnergyCalculator energyCalculator;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final Clock clock;

    public WhoopSyncService(
            WhoopProperties properties,
            WhoopClient whoopClient,
            WhoopOAuthService oAuthService,
            WhoopConnectionStateRecorder stateRecorder,
            WhoopConnectionRepository connectionRepository,
            WhoopCycleRepository cycleRepository,
            WhoopRecoveryRepository recoveryRepository,
            WhoopSleepRepository sleepRepository,
            WhoopWorkoutRepository workoutRepository,
            TrainingSessionRepository trainingSessionRepository,
            BodyMeasurementRepository bodyMeasurementRepository,
            UserService userService,
            EnergyCalculator energyCalculator,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper,
            Clock clock) {
        this.properties = properties;
        this.whoopClient = whoopClient;
        this.oAuthService = oAuthService;
        this.stateRecorder = stateRecorder;
        this.connectionRepository = connectionRepository;
        this.cycleRepository = cycleRepository;
        this.recoveryRepository = recoveryRepository;
        this.sleepRepository = sleepRepository;
        this.workoutRepository = workoutRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.bodyMeasurementRepository = bodyMeasurementRepository;
        this.userService = userService;
        this.energyCalculator = energyCalculator;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public WhoopSyncResult sync(UUID userId) {
        WhoopConnection connection = connectionRepository
                .findByUserId(userId)
                .orElseThrow(() -> new com.fittrack.common.exception.NotFoundException(
                        "No WHOOP connection for this user"));
        return sync(connection);
    }

    @Transactional
    public WhoopSyncResult sync(WhoopConnection connection) {
        Instant now = clock.instant();
        UUID userId = connection.getUser().getId();
        boolean initialSync = connection.getLastSyncedThrough() == null;

        Instant windowStart = initialSync
                ? now.minus(Duration.ofDays(properties.getInitialSyncDays()))
                : connection.getLastSyncedThrough().minus(properties.getIncrementalSyncOverlap());
        Instant windowEnd = now;

        String accessToken = oAuthService.accessTokenFor(connection);
        ZoneId zone = userService.zoneOf(userId);
        User user = connection.getUser();

        try {
            Counts cycles = syncCycles(user, accessToken, windowStart, windowEnd, zone);
            Counts recoveries = syncRecoveries(user, accessToken, windowStart, windowEnd, zone);
            Counts sleeps = syncSleeps(user, accessToken, windowStart, windowEnd, zone);
            WorkoutCounts workouts = syncWorkouts(user, accessToken, windowStart, windowEnd, zone);
            int measurements = initialSync ? syncBodyMeasurement(user, accessToken, now) : 0;

            connection.recordSyncSuccess(now, windowEnd);
            connectionRepository.save(connection);

            WhoopSyncResult result = new WhoopSyncResult(
                    now,
                    windowStart,
                    windowEnd,
                    cycles.created(),
                    cycles.updated(),
                    recoveries.created(),
                    recoveries.updated(),
                    sleeps.created(),
                    sleeps.updated(),
                    workouts.created(),
                    workouts.updated(),
                    workouts.sessionsCreated(),
                    measurements,
                    initialSync);

            log.info(
                    "WHOOP sync for user {} imported {} and updated {} records",
                    userId,
                    result.totalImported(),
                    result.totalUpdated());
            return result;
        } catch (WhoopApiException ex) {
            // Recorded in its own transaction: rethrowing rolls this one back, and the user would
            // otherwise never see why their sync stopped working.
            stateRecorder.recordFailure(
                    connection.getId(), ex.getMessage(), ex.isAuthorisationFailure());
            throw ex;
        }
    }

    // --------------------------------------------------------------- entities

    private Counts syncCycles(User user, String token, Instant from, Instant to, ZoneId zone) {
        int created = 0;
        int updated = 0;
        for (JsonNode record : whoopClient.getCycles(token, from, to)) {
            Long cycleId = WhoopJson.longValue(record, "id");
            if (cycleId == null) {
                continue;
            }
            Optional<WhoopCycle> existing =
                    cycleRepository.findByUserIdAndWhoopCycleId(user.getId(), cycleId);
            WhoopCycle cycle = existing.orElseGet(() -> new WhoopCycle(user, cycleId));

            Instant start = WhoopJson.instant(record, "start");
            cycle.setStartAt(start != null ? start : clock.instant());
            cycle.setEndAt(WhoopJson.instant(record, "end"));
            cycle.setCycleDate(LocalDate.ofInstant(cycle.getStartAt(), zone));
            cycle.setTimezoneOffset(WhoopJson.text(record, "timezone_offset"));
            cycle.setScoreState(WhoopJson.text(record, "score_state"));
            cycle.setStrain(WhoopJson.decimal(record, 2, "score", "strain"));
            cycle.setKilojoules(WhoopJson.decimal(record, 2, "score", "kilojoule"));
            cycle.setAverageHeartRate(WhoopJson.intValue(record, "score", "average_heart_rate"));
            cycle.setMaxHeartRate(WhoopJson.intValue(record, "score", "max_heart_rate"));
            cycle.setRawPayload(record);

            cycleRepository.save(cycle);
            if (existing.isPresent()) {
                updated++;
            } else {
                created++;
            }
        }
        return new Counts(created, updated);
    }

    private Counts syncRecoveries(User user, String token, Instant from, Instant to, ZoneId zone) {
        int created = 0;
        int updated = 0;
        for (JsonNode record : whoopClient.getRecoveries(token, from, to)) {
            Long cycleId = WhoopJson.longValue(record, "cycle_id");
            if (cycleId == null) {
                continue;
            }
            Optional<WhoopRecovery> existing =
                    recoveryRepository.findByUserIdAndWhoopCycleId(user.getId(), cycleId);
            WhoopRecovery recovery = existing.orElseGet(() -> new WhoopRecovery(user, cycleId));

            Instant recordedAt = WhoopJson.instant(record, "created_at");
            recovery.setRecordedAt(recordedAt);
            // A recovery belongs to the day of its cycle; fall back to the record timestamp.
            LocalDate date = cycleRepository
                    .findByUserIdAndWhoopCycleId(user.getId(), cycleId)
                    .map(WhoopCycle::getCycleDate)
                    .orElseGet(() -> LocalDate.ofInstant(
                            recordedAt != null ? recordedAt : clock.instant(), zone));
            recovery.setRecoveryDate(date);
            recovery.setWhoopSleepId(WhoopJson.text(record, "sleep_id"));
            recovery.setScoreState(WhoopJson.text(record, "score_state"));
            recovery.setUserCalibrating(WhoopJson.bool(record, "score", "user_calibrating"));
            recovery.setRecoveryScore(WhoopJson.roundedInt(record, "score", "recovery_score"));
            recovery.setRestingHeartRate(WhoopJson.decimal(record, 2, "score", "resting_heart_rate"));
            recovery.setHrvRmssdMilli(WhoopJson.decimal(record, 3, "score", "hrv_rmssd_milli"));
            recovery.setSpo2Percentage(WhoopJson.decimal(record, 2, "score", "spo2_percentage"));
            recovery.setSkinTempCelsius(WhoopJson.decimal(record, 2, "score", "skin_temp_celsius"));
            recovery.setRawPayload(record);

            recoveryRepository.save(recovery);
            if (existing.isPresent()) {
                updated++;
            } else {
                created++;
            }
        }
        return new Counts(created, updated);
    }

    private Counts syncSleeps(User user, String token, Instant from, Instant to, ZoneId zone) {
        int created = 0;
        int updated = 0;
        for (JsonNode record : whoopClient.getSleeps(token, from, to)) {
            String sleepId = WhoopJson.text(record, "id");
            if (sleepId == null) {
                continue;
            }
            Optional<WhoopSleep> existing =
                    sleepRepository.findByUserIdAndWhoopSleepId(user.getId(), sleepId);
            WhoopSleep sleep = existing.orElseGet(() -> new WhoopSleep(user, sleepId));

            Instant start = WhoopJson.instant(record, "start");
            Instant end = WhoopJson.instant(record, "end");
            sleep.setStartAt(start != null ? start : clock.instant());
            sleep.setEndAt(end);
            // A night is attributed to the day it ended on, which is how a user thinks of it.
            sleep.setSleepDate(LocalDate.ofInstant(end != null ? end : sleep.getStartAt(), zone));
            sleep.setWhoopCycleId(WhoopJson.longValue(record, "cycle_id"));
            sleep.setTimezoneOffset(WhoopJson.text(record, "timezone_offset"));
            sleep.setNap(Boolean.TRUE.equals(WhoopJson.bool(record, "nap")));
            sleep.setScoreState(WhoopJson.text(record, "score_state"));

            Long inBed = WhoopJson.longValue(record, "score", "stage_summary", "total_in_bed_time_milli");
            Long awake = WhoopJson.longValue(record, "score", "stage_summary", "total_awake_time_milli");
            sleep.setTotalInBedTimeMillis(inBed);
            sleep.setTotalAwakeTimeMillis(awake);
            sleep.setTotalLightSleepTimeMillis(
                    WhoopJson.longValue(record, "score", "stage_summary", "total_light_sleep_time_milli"));
            sleep.setTotalSlowWaveSleepTimeMillis(WhoopJson.longValue(
                    record, "score", "stage_summary", "total_slow_wave_sleep_time_milli"));
            sleep.setTotalRemSleepTimeMillis(
                    WhoopJson.longValue(record, "score", "stage_summary", "total_rem_sleep_time_milli"));
            sleep.setDisturbanceCount(
                    WhoopJson.intValue(record, "score", "stage_summary", "disturbance_count"));
            sleep.setSleepCycleCount(
                    WhoopJson.intValue(record, "score", "stage_summary", "sleep_cycle_count"));
            sleep.setSleepDurationMillis(actualSleepMillis(inBed, awake, start, end));
            sleep.setSleepNeedMillis(sleepNeedMillis(record));
            sleep.setRespiratoryRate(WhoopJson.decimal(record, 3, "score", "respiratory_rate"));
            sleep.setSleepPerformancePercentage(
                    WhoopJson.roundedInt(record, "score", "sleep_performance_percentage"));
            sleep.setSleepConsistencyPercentage(
                    WhoopJson.roundedInt(record, "score", "sleep_consistency_percentage"));
            sleep.setSleepEfficiencyPercentage(
                    WhoopJson.decimal(record, 3, "score", "sleep_efficiency_percentage"));
            sleep.setRawPayload(record);

            sleepRepository.save(sleep);
            if (existing.isPresent()) {
                updated++;
            } else {
                created++;
            }
        }
        return new Counts(created, updated);
    }

    private WorkoutCounts syncWorkouts(User user, String token, Instant from, Instant to, ZoneId zone) {
        int created = 0;
        int updated = 0;
        int sessionsCreated = 0;

        for (JsonNode record : whoopClient.getWorkouts(token, from, to)) {
            String workoutId = WhoopJson.text(record, "id");
            if (workoutId == null) {
                continue;
            }
            Optional<WhoopWorkout> existing =
                    workoutRepository.findByUserIdAndWhoopWorkoutId(user.getId(), workoutId);
            WhoopWorkout workout = existing.orElseGet(() -> new WhoopWorkout(user, workoutId));

            Instant start = WhoopJson.instant(record, "start");
            Instant end = WhoopJson.instant(record, "end");
            workout.setStartAt(start != null ? start : clock.instant());
            workout.setEndAt(end);
            workout.setWorkoutDate(LocalDate.ofInstant(workout.getStartAt(), zone));
            workout.setTimezoneOffset(WhoopJson.text(record, "timezone_offset"));
            workout.setSportId(WhoopJson.intValue(record, "sport_id"));
            workout.setSportName(WhoopSportCatalog.sportName(
                    WhoopJson.text(record, "sport_name"), workout.getSportId()));
            workout.setScoreState(WhoopJson.text(record, "score_state"));
            workout.setStrain(WhoopJson.decimal(record, 2, "score", "strain"));
            workout.setKilojoules(WhoopJson.decimal(record, 2, "score", "kilojoule"));
            workout.setAverageHeartRate(WhoopJson.intValue(record, "score", "average_heart_rate"));
            workout.setMaxHeartRate(WhoopJson.intValue(record, "score", "max_heart_rate"));
            workout.setDistanceMeters(WhoopJson.decimal(record, 3, "score", "distance_meter"));
            workout.setAltitudeGainMeters(WhoopJson.decimal(record, 3, "score", "altitude_gain_meter"));
            workout.setPercentRecorded(WhoopJson.decimal(record, 2, "score", "percent_recorded"));
            workout.setRawPayload(record);

            boolean newSession = projectIntoTrainingSession(user, workout, zone);
            if (newSession) {
                sessionsCreated++;
            }

            workoutRepository.save(workout);
            if (existing.isPresent()) {
                updated++;
            } else {
                created++;
            }
        }
        return new WorkoutCounts(created, updated, sessionsCreated);
    }

    /**
     * Mirrors a WHOOP workout into the training log. Returns true when a session was created.
     *
     * <p>Only device-measured fields are (re)written. A title, category, exertion rating or note
     * the user has added survives every subsequent sync - re-importing must never overwrite the
     * user's own input.
     */
    private boolean projectIntoTrainingSession(User user, WhoopWorkout workout, ZoneId zone) {
        Optional<TrainingSession> existing = trainingSessionRepository.findByUserIdAndSourceAndExternalId(
                user.getId(), TrainingSource.WHOOP, workout.getWhoopWorkoutId());

        boolean isNew = existing.isEmpty();
        TrainingSession session = existing.orElseGet(() -> {
            TrainingSession fresh = new TrainingSession(
                    user,
                    workout.getSportName(),
                    workout.getStartAt(),
                    LocalDate.ofInstant(workout.getStartAt(), zone));
            fresh.setSource(TrainingSource.WHOOP);
            fresh.setExternalId(workout.getWhoopWorkoutId());
            fresh.setCategory(WhoopSportCatalog.categoryFor(workout.getSportName()));
            return fresh;
        });

        session.setStartedAt(workout.getStartAt());
        session.setEndedAt(workout.getEndAt());
        session.setSessionDate(LocalDate.ofInstant(workout.getStartAt(), zone));
        session.setSportLabel(workout.getSportName());
        session.setDurationMinutes(durationMinutes(workout));
        session.setStrain(workout.getStrain());
        session.setAverageHeartRate(workout.getAverageHeartRate());
        session.setMaxHeartRate(workout.getMaxHeartRate());
        session.setDistanceMeters(workout.getDistanceMeters());
        session.setCaloriesKcal(energyCalculator.kilojoulesToKilocalories(workout.getKilojoules()));

        TrainingSession saved = trainingSessionRepository.save(session);
        workout.setTrainingSessionId(saved.getId());
        return isNew;
    }

    /** WHOOP reports body weight and height on the profile rather than as a time series. */
    private int syncBodyMeasurement(User user, String accessToken, Instant now) {
        try {
            JsonNode body = whoopClient.getBodyMeasurement(accessToken);
            BigDecimal weightKg = WhoopJson.decimal(body, 2, "weight_kilogram");
            if (weightKg == null || weightKg.signum() <= 0) {
                return 0;
            }
            // One imported row per WHOOP profile snapshot, keyed so a re-sync cannot duplicate it.
            String externalId = "profile-" + now.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
            if (bodyMeasurementRepository
                    .findByUserIdAndSourceAndExternalId(user.getId(), MeasurementSource.WHOOP, externalId)
                    .isPresent()) {
                return 0;
            }
            BodyMeasurement measurement =
                    new BodyMeasurement(user, now, weightKg, MeasurementSource.WHOOP);
            measurement.setExternalId(externalId);
            measurement.setNote("Imported from your WHOOP profile");
            bodyMeasurementRepository.save(measurement);
            return 1;
        } catch (WhoopApiException ex) {
            // Body measurement is optional data; a failure here must not fail the whole sync.
            log.debug("Could not import WHOOP body measurement: {}", ex.getMessage());
            return 0;
        }
    }

    // ---------------------------------------------------------------- helpers

    private static int durationMinutes(WhoopWorkout workout) {
        if (workout.getEndAt() == null) {
            return 0;
        }
        long minutes = Duration.between(workout.getStartAt(), workout.getEndAt()).toMinutes();
        return (int) Math.clamp(minutes, 0, 1440);
    }

    /**
     * Time actually asleep. WHOOP reports time in bed and time awake; where the stage summary is
     * missing, fall back to the wall-clock span.
     */
    private static Long actualSleepMillis(Long inBed, Long awake, Instant start, Instant end) {
        if (inBed != null) {
            long asleep = inBed - (awake == null ? 0 : awake);
            return asleep > 0 ? asleep : inBed;
        }
        if (start != null && end != null) {
            return Duration.between(start, end).toMillis();
        }
        return null;
    }

    /** Total sleep need: the baseline plus debt, recent strain and nap adjustments. */
    private static Long sleepNeedMillis(JsonNode record) {
        Long baseline = WhoopJson.longValue(record, "score", "sleep_needed", "baseline_milli");
        if (baseline == null) {
            return null;
        }
        long total = baseline;
        Long debt = WhoopJson.longValue(record, "score", "sleep_needed", "need_from_sleep_debt_milli");
        Long strain = WhoopJson.longValue(record, "score", "sleep_needed", "need_from_recent_strain_milli");
        Long nap = WhoopJson.longValue(record, "score", "sleep_needed", "need_from_recent_nap_milli");
        if (debt != null) {
            total += debt;
        }
        if (strain != null) {
            total += strain;
        }
        // A recent nap reduces the remaining need, and WHOOP reports it as a negative value.
        if (nap != null) {
            total += nap;
        }
        return total > 0 ? total : baseline;
    }

    List<WhoopConnection> connectionsToSync() {
        return connectionRepository.findByStatus(
                com.fittrack.whoop.domain.WhoopConnectionStatus.CONNECTED);
    }

    private record Counts(int created, int updated) {}

    private record WorkoutCounts(int created, int updated, int sessionsCreated) {}
}
