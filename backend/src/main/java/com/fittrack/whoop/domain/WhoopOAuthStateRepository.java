package com.fittrack.whoop.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WhoopOAuthStateRepository extends JpaRepository<WhoopOAuthState, UUID> {

    Optional<WhoopOAuthState> findByState(String state);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from WhoopOAuthState s where s.expiresAt < :cutoff")
    int deleteExpired(@Param("cutoff") Instant cutoff);
}
