package com.fittrack.analytics.service;

import java.util.UUID;

/** Lets the dashboard tell "no data yet" apart from "no device connected". */
public interface WearableConnectionStatusPort {

    boolean isConnected(UUID userId);
}
