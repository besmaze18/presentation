package com.fittrack.whoop.domain;

public enum WhoopConnectionStatus {
    CONNECTED,
    /** The stored grant no longer works; the user must go through the OAuth flow again. */
    REAUTHORISATION_REQUIRED,
    REVOKED
}
