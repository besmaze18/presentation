package com.fittrack.ai.domain;

public enum AiAnalysisStatus {
    /** The provider returned a valid prediction that is waiting for the user to review it. */
    PENDING_REVIEW,
    /** The user reviewed the prediction and saved a food entry from it. */
    CONFIRMED,
    /** The user discarded the prediction. */
    DISCARDED,
    /** The provider failed or returned something unusable. */
    FAILED
}
