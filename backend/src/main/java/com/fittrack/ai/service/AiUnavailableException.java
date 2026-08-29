package com.fittrack.ai.service;

import com.fittrack.common.exception.ApiException;
import org.springframework.http.HttpStatus;

/**
 * Signals that AI analysis could not produce a trustworthy result. Mapped to 503 so the client can
 * fall back to manual entry rather than treating it as a bug.
 */
public class AiUnavailableException extends ApiException {

    public AiUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", message);
    }

    public AiUnavailableException(String message, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", message, cause);
    }
}
