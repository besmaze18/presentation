package com.fittrack.user.service;

import com.fittrack.common.exception.NotFoundException;
import com.fittrack.common.util.Numbers;
import com.fittrack.user.domain.BodyMeasurement;
import com.fittrack.user.domain.BodyMeasurementRepository;
import com.fittrack.user.domain.MeasurementSource;
import com.fittrack.user.dto.BodyMeasurementResponse;
import com.fittrack.user.dto.UpsertBodyMeasurementRequest;
import com.fittrack.user.dto.WeightTrendResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BodyMeasurementService {

    private final BodyMeasurementRepository repository;
    private final UserService userService;
    private final Clock clock;

    public BodyMeasurementService(
            BodyMeasurementRepository repository, UserService userService, Clock clock) {
        this.repository = repository;
        this.userService = userService;
        this.clock = clock;
    }

    @Transactional
    public BodyMeasurementResponse record(UUID userId, UpsertBodyMeasurementRequest request) {
        Instant recordedAt = request.recordedAt() != null ? request.recordedAt() : clock.instant();
        BodyMeasurement measurement = new BodyMeasurement(
                userService.requireUser(userId), recordedAt, request.weightKg(), MeasurementSource.MANUAL);
        measurement.setBodyFatPercentage(request.bodyFatPercentage());
        measurement.setNote(request.note());
        return BodyMeasurementResponse.from(repository.save(measurement));
    }

    @Transactional
    public BodyMeasurementResponse update(UUID userId, UUID id, UpsertBodyMeasurementRequest request) {
        BodyMeasurement measurement = require(userId, id);
        if (request.recordedAt() != null) {
            measurement.setRecordedAt(request.recordedAt());
        }
        measurement.setWeightKg(request.weightKg());
        measurement.setBodyFatPercentage(request.bodyFatPercentage());
        measurement.setNote(request.note());
        return BodyMeasurementResponse.from(repository.save(measurement));
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        repository.delete(require(userId, id));
    }

    @Transactional(readOnly = true)
    public Page<BodyMeasurementResponse> list(UUID userId, int page, int size) {
        return repository
                .findByUserIdOrderByRecordedAtDesc(
                        userId, PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 200)))
                .map(BodyMeasurementResponse::from);
    }

    @Transactional(readOnly = true)
    public List<BodyMeasurement> between(UUID userId, Instant from, Instant toExclusive) {
        return repository
                .findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        userId, from, toExclusive);
    }

    @Transactional(readOnly = true)
    public Optional<BodyMeasurement> latest(UUID userId) {
        return repository.findFirstByUserIdOrderByRecordedAtDesc(userId);
    }

    /** The most recent weight recorded at or before the end of the given day, if any. */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> weightOn(UUID userId, LocalDate date, ZoneId zone) {
        Instant endOfDay = date.plusDays(1).atStartOfDay(zone).toInstant();
        return repository
                .findFirstByUserIdAndRecordedAtLessThanOrderByRecordedAtDesc(userId, endOfDay)
                .map(BodyMeasurement::getWeightKg);
    }

    /**
     * Rolling averages and their period-over-period change. Comparing averages rather than two
     * single readings is what makes a 0.4 kg trend meaningful rather than noise.
     */
    @Transactional(readOnly = true)
    public WeightTrendResponse trend(UUID userId, LocalDate today, ZoneId zone, BigDecimal targetKg) {
        Optional<BodyMeasurement> latest = latest(userId);
        Instant now = today.plusDays(1).atStartOfDay(zone).toInstant();

        BigDecimal average7 = averageOverDays(userId, now, zone, 0, 7);
        BigDecimal previous7 = averageOverDays(userId, now, zone, 7, 14);
        BigDecimal average30 = averageOverDays(userId, now, zone, 0, 30);
        BigDecimal previous30 = averageOverDays(userId, now, zone, 30, 60);

        BigDecimal latestKg = latest.map(BodyMeasurement::getWeightKg).orElse(null);
        BigDecimal toTarget = latestKg != null && targetKg != null
                ? Numbers.oneDecimal(latestKg.subtract(targetKg))
                : null;

        return new WeightTrendResponse(
                latestKg == null ? null : Numbers.oneDecimal(latestKg),
                latest.map(BodyMeasurement::getRecordedAt).orElse(null),
                average7,
                average30,
                difference(average7, previous7),
                difference(average30, previous30),
                targetKg == null ? null : Numbers.oneDecimal(targetKg),
                toTarget);
    }

    /** Mean weight over the window [now - toDaysAgo, now - fromDaysAgo). */
    private BigDecimal averageOverDays(
            UUID userId, Instant now, ZoneId zone, int fromDaysAgo, int toDaysAgo) {
        Instant windowEnd = now.minus(java.time.Duration.ofDays(fromDaysAgo));
        Instant windowStart = now.minus(java.time.Duration.ofDays(toDaysAgo));
        List<BodyMeasurement> measurements = repository
                .findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
                        userId, windowStart, windowEnd);
        if (measurements.isEmpty()) {
            return null;
        }
        BigDecimal sum = measurements.stream()
                .map(BodyMeasurement::getWeightKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(measurements.size()), 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal difference(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null) {
            return null;
        }
        return Numbers.oneDecimal(current.subtract(previous));
    }

    private BodyMeasurement require(UUID userId, UUID id) {
        return repository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> NotFoundException.of("Body measurement", id));
    }
}
