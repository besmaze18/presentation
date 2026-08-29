package com.fittrack.storage.service;

import java.time.Duration;
import java.util.Optional;

/**
 * The application's only view of object storage.
 *
 * <p>Domain code (nutrition, AI) depends on this interface and never on a provider SDK, so an
 * adapter can be swapped without touching a single domain class.
 */
public interface StorageService {

    /** Provider name recorded alongside each stored reference. */
    String providerName();

    StoredObject upload(StorageUpload upload);

    /** Reads the bytes back, or empty when the object no longer exists. */
    Optional<byte[]> retrieve(String key);

    void delete(String key);

    /**
     * A URL a browser can load directly. Where the provider supports it this is a signed,
     * time-limited URL; otherwise it is a stable delivery URL.
     */
    String accessibleUrl(String key, Duration ttl);
}
