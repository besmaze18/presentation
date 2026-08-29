package com.fittrack.training.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, UUID> {

    /**
     * Only the exercises are fetched in the same query. Fetching their sets here too would be two
     * list joins in one statement, which Hibernate rejects; the sets are batch-loaded instead
     * (see {@code @BatchSize} on Exercise.sets), which avoids an N+1 without the illegal join.
     */
    @EntityGraph(attributePaths = "exercises")
    Optional<TrainingSession> findByIdAndUserId(UUID id, UUID userId);

    Page<TrainingSession> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    List<TrainingSession> findByUserIdAndSessionDateOrderByStartedAtAsc(UUID userId, LocalDate date);

    List<TrainingSession> findByUserIdAndSessionDateBetweenOrderByStartedAtAsc(
            UUID userId, LocalDate from, LocalDate to);

    /** Idempotency check for wearable synchronisation. */
    Optional<TrainingSession> findByUserIdAndSourceAndExternalId(
            UUID userId, TrainingSource source, String externalId);

    @Query(
            """
            select new com.fittrack.training.domain.DailyTrainingTotals(
                s.sessionDate,
                count(s),
                coalesce(sum(s.durationMinutes), 0),
                coalesce(sum(s.caloriesKcal), 0),
                coalesce(max(s.strain), 0))
            from TrainingSession s
            where s.user.id = :userId and s.sessionDate between :from and :to
            group by s.sessionDate
            order by s.sessionDate
            """)
    List<DailyTrainingTotals> aggregateByDay(
            @Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
