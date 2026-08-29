package com.fittrack.whoop.client;

import com.fittrack.common.exception.ExternalServiceException;

/** A WHOOP API failure, carrying the status so callers can distinguish auth loss from an outage. */
public class WhoopApiException extends ExternalServiceException {

    private final int statusCode;

    public WhoopApiException(int statusCode, String message) {
        super("whoop", message);
        this.statusCode = statusCode;
    }

    public WhoopApiException(int statusCode, String message, Throwable cause) {
        super("whoop", message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** 401/403 mean the stored grant is no longer usable and the user must reconnect. */
    public boolean isAuthorisationFailure() {
        return statusCode == 401 || statusCode == 403;
    }
}
