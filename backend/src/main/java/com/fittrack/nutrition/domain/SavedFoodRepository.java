package com.fittrack.nutrition.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SavedFoodRepository extends JpaRepository<SavedFood, UUID> {

    Optional<SavedFood> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);

    /** Most-used first, so the foods a user actually eats surface without typing. */
    @Query(
            """
            select f from SavedFood f
            where f.user.id = :userId
              and (:query is null or lower(f.name) like lower(concat('%', :query, '%'))
                   or lower(coalesce(f.brand, '')) like lower(concat('%', :query, '%')))
            order by f.usageCount desc, f.lastUsedAt desc nulls last, f.name asc
            """)
    List<SavedFood> search(
            @Param("userId") UUID userId, @Param("query") String query, Pageable pageable);
}
