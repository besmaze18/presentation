package com.fittrack.whoop.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhoopRecoveryRepository extends JpaRepository<WhoopRecovery, UUID> {

    Optional<WhoopRecovery> findByUserIdAndWhoopCycleId(UUID userId, Long whoopCycleId);

    List<WhoopRecovery> findByUserIdAndRecoveryDateBetweenOrderByRecoveryDateAsc(
            UUID userId, LocalDate from, LocalDate to);

    Optional<WhoopRecovery> findFirstByUserIdAndRecoveryDateOrderByRecordedAtDesc(
            UUID userId, LocalDate date);

    long countByUserId(UUID userId);
}
