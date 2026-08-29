package com.fittrack.whoop.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WhoopConnectionRepository extends JpaRepository<WhoopConnection, UUID> {

    Optional<WhoopConnection> findByUserId(UUID userId);

    Optional<WhoopConnection> findByWhoopUserId(Long whoopUserId);

    List<WhoopConnection> findByStatus(WhoopConnectionStatus status);
}
