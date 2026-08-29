package com.fittrack.storage.service;

import java.time.Instant;

/**
 * A reference to a stored file. Only this reference and its metadata are persisted in PostgreSQL -
 * never the bytes themselves.
 */
public record StoredObject(
        String provider,
        /** Provider-scoped identifier, e.g. an object key or public id. */
        String key,
        String contentType,
        long sizeBytes,
        Instant storedAt) {}
