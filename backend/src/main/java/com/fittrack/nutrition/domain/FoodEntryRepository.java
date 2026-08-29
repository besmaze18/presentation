package com.fittrack.nutrition.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodEntryRepository extends JpaRepository<FoodEntry, UUID> {

    /**
     * Always look an entry up by id <em>and</em> owner: this is the single choke point that stops
     * one user reading or mutating another user's rows.
     */
    @EntityGraph(attributePaths = "items")
    Optional<FoodEntry> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = "items")
    List<FoodEntry> findByUserIdAndEntryDateOrderByConsumedAtAsc(UUID userId, LocalDate entryDate);

    @EntityGraph(attributePaths = "items")
    List<FoodEntry> findByUserIdAndEntryDateBetweenOrderByConsumedAtAsc(
            UUID userId, LocalDate from, LocalDate to);

    List<FoodEntry> findByUserIdOrderByConsumedAtDesc(UUID userId, Limit limit);

    long countByUserIdAndEntryDate(UUID userId, LocalDate entryDate);

    /** One row per day that has at least one entry, aggregated in the database. */
    @Query(
            """
            select new com.fittrack.nutrition.domain.DailyMacroTotals(
                e.entryDate,
                coalesce(sum(e.macros.calories), 0),
                coalesce(sum(e.macros.proteinG), 0),
                coalesce(sum(e.macros.carbsG), 0),
                coalesce(sum(e.macros.fatG), 0),
                coalesce(sum(e.macros.fiberG), 0),
                count(e))
            from FoodEntry e
            where e.user.id = :userId and e.entryDate between :from and :to
            group by e.entryDate
            order by e.entryDate
            """)
    List<DailyMacroTotals> aggregateByDay(
            @Param("userId") UUID userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(
            """
            select new com.fittrack.nutrition.domain.DailyMacroTotals(
                :date,
                coalesce(sum(e.macros.calories), 0),
                coalesce(sum(e.macros.proteinG), 0),
                coalesce(sum(e.macros.carbsG), 0),
                coalesce(sum(e.macros.fatG), 0),
                coalesce(sum(e.macros.fiberG), 0),
                count(e))
            from FoodEntry e
            where e.user.id = :userId and e.entryDate = :date
            """)
    DailyMacroTotals aggregateForDay(@Param("userId") UUID userId, @Param("date") LocalDate date);
}
