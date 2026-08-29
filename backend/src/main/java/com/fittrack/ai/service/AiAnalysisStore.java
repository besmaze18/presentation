package com.fittrack.ai.service;

import com.fittrack.ai.domain.AiAnalysis;
import com.fittrack.ai.domain.AiAnalysisRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists an analysis in steps that each commit on their own.
 *
 * <p>An image analysis stores the photo and the attempt, calls a provider that may fail, and then
 * records the outcome. If all three shared one transaction, a provider failure would roll the
 * whole thing back — losing the record of the attempt and orphaning the uploaded image, which is
 * the opposite of the intent. Committing the pending analysis before the provider call, and the
 * outcome after it, is what makes "the image survives a failed analysis" actually true.
 */
@Component
public class AiAnalysisStore {

    private final AiAnalysisRepository repository;

    public AiAnalysisStore(AiAnalysisRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AiAnalysis save(AiAnalysis analysis) {
        return repository.saveAndFlush(analysis);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(UUID analysisId, String message) {
        repository.findById(analysisId).ifPresent(analysis -> {
            analysis.markFailed(message);
            repository.saveAndFlush(analysis);
        });
    }
}
