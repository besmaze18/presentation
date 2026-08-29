package com.fittrack.ai.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, UUID> {

    Optional<AiAnalysis> findByIdAndUserId(UUID id, UUID userId);

    List<AiAnalysis> findByUserIdOrderByCreatedAtDesc(UUID userId, Limit limit);

    List<AiAnalysis> findByUserIdAndStatusOrderByCreatedAtDesc(
            UUID userId, AiAnalysisStatus status, Limit limit);
}
