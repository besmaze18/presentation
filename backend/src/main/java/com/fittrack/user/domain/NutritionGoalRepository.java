package com.fittrack.user.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionGoalRepository extends JpaRepository<NutritionGoal, UUID> {

    /** The goal in effect on a given day: the most recent row not starting after that day. */
    Optional<NutritionGoal> findFirstByUserIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            UUID userId, LocalDate date);

    Optional<NutritionGoal> findByUserIdAndEffectiveFrom(UUID userId, LocalDate effectiveFrom);

    List<NutritionGoal> findByUserIdOrderByEffectiveFromDesc(UUID userId, Limit limit);
}
