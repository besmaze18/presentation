package com.fittrack.whoop.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhoopCycleRepository extends JpaRepository<WhoopCycle, UUID> {

    /** The idempotency key: a cycle id is unique within a user. */
    Optional<WhoopCycle> findByUserIdAndWhoopCycleId(UUID userId, Long whoopCycleId);

    List<WhoopCycle> findByUserIdAndCycleDateBetweenOrderByCycleDateAsc(
            UUID userId, LocalDate from, LocalDate to);

    Optional<WhoopCycle> findFirstByUserIdAndCycleDateOrderByStartAtDesc(UUID userId, LocalDate date);

    long countByUserId(UUID userId);
}
