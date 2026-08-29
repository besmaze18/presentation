package com.fittrack.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Raised when a third-party integration (WHOOP, the AI provider, object storage) fails.
 * Callers are expected to degrade gracefully rather than surface a 500 to the user.
 */
public class ExternalServiceException extends ApiException {

    private final String service;

    public ExternalServiceException(String service, String message) {
        super(HttpStatus.BAD_GATEWAY, "EXTERNAL_SERVICE_ERROR", message);
        this.service = service;
    }

    public ExternalServiceException(String service, String message, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY, "EXTERNAL_SERVICE_ERROR", message, cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
