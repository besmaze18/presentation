package com.fittrack.whoop.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhoopWorkoutRepository extends JpaRepository<WhoopWorkout, UUID> {

    Optional<WhoopWorkout> findByUserIdAndWhoopWorkoutId(UUID userId, String whoopWorkoutId);

    List<WhoopWorkout> findByUserIdAndWorkoutDateBetweenOrderByStartAtAsc(
            UUID userId, LocalDate from, LocalDate to);

    long countByUserId(UUID userId);
}
