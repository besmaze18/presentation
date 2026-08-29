package com.fittrack.user.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, UUID> {

    Optional<BodyMeasurement> findByIdAndUserId(UUID id, UUID userId);

    Page<BodyMeasurement> findByUserIdOrderByRecordedAtDesc(UUID userId, Pageable pageable);

    Optional<BodyMeasurement> findFirstByUserIdOrderByRecordedAtDesc(UUID userId);

    Optional<BodyMeasurement> findFirstByUserIdAndRecordedAtLessThanOrderByRecordedAtDesc(
            UUID userId, Instant before);

    List<BodyMeasurement> findByUserIdAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtAsc(
            UUID userId, Instant from, Instant toExclusive);

    Optional<BodyMeasurement> findByUserIdAndSourceAndExternalId(
            UUID userId, MeasurementSource source, String externalId);
}
