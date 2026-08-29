package com.fittrack.storage.config;

import com.fittrack.storage.adapter.CloudJsStorageAdapter;
import com.fittrack.storage.adapter.LocalFilesystemStorageAdapter;
import com.fittrack.storage.service.StorageService;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Selects the active storage adapter. Both adapters are constructed behind the same interface, so
 * switching provider is a configuration change rather than a code change.
 */
@Configuration
public class StorageConfiguration {

    private static final Logger log = LoggerFactory.getLogger(StorageConfiguration.class);

    @Bean
    @Primary
    public StorageService storageService(
            StorageProperties properties, LocalFilesystemStorageAdapter localAdapter, Clock clock) {

        if ("cloudjs".equalsIgnoreCase(properties.getProvider())) {
            if (!properties.getCloudjs().isConfigured()) {
                // Falling back keeps the application usable rather than failing to start with a
                // half-configured integration; the warning makes the downgrade impossible to miss.
                log.warn(
                        "Storage provider 'cloudjs' selected but CLOUDJS_CLOUD_NAME / CLOUDJS_API_KEY / "
                                + "CLOUDJS_API_SECRET are not all set - falling back to local filesystem storage");
                return localAdapter;
            }
            log.info("Using Cloud.js object storage");
            return new CloudJsStorageAdapter(properties.getCloudjs(), clock);
        }

        log.info("Using local filesystem storage at {}", properties.getLocal().getDirectory());
        return localAdapter;
    }
}
