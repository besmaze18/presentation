package com.fittrack.whoop.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhoopSleepRepository extends JpaRepository<WhoopSleep, UUID> {

    Optional<WhoopSleep> findByUserIdAndWhoopSleepId(UUID userId, String whoopSleepId);

    List<WhoopSleep> findByUserIdAndSleepDateBetweenAndNapFalseOrderBySleepDateAsc(
            UUID userId, LocalDate from, LocalDate to);

    /** The main (non-nap) sleep attributed to a day. */
    Optional<WhoopSleep> findFirstByUserIdAndSleepDateAndNapFalseOrderBySleepDurationMillisDesc(
            UUID userId, LocalDate date);

    long countByUserId(UUID userId);
}
