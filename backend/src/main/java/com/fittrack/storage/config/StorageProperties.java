package com.fittrack.storage.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fittrack.storage")
public class StorageProperties {

    /** Which adapter to activate: "local" or "cloudjs". */
    private String provider = "local";

    private long maxFileSizeBytes = 10L * 1024 * 1024;

    private Duration signedUrlTtl = Duration.ofMinutes(15);

    private Local local = new Local();

    private CloudJs cloudjs = new CloudJs();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public Duration getSignedUrlTtl() {
        return signedUrlTtl;
    }

    public void setSignedUrlTtl(Duration signedUrlTtl) {
        this.signedUrlTtl = signedUrlTtl;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public CloudJs getCloudjs() {
        return cloudjs;
    }

    public void setCloudjs(CloudJs cloudjs) {
        this.cloudjs = cloudjs;
    }

    public static class Local {
        private String directory = "./data/uploads";
        private String publicBaseUrl = "http://localhost:8080/api/storage/files";

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }

    /**
     * Cloud.js object-storage configuration.
     *
     * <p>See docs/ARCHITECTURE.md: the concrete HTTP contract implemented here is the
     * Cloudinary-compatible upload API (signed {@code POST /{cloud_name}/image/upload}, signed
     * destroy, and signed delivery URLs), which is what "Cloud.js" resolves to for object storage.
     * If your Cloud.js deployment exposes a different contract, replace only the adapter - nothing
     * outside {@code com.fittrack.storage.adapter} knows these details.
     */
    public static class CloudJs {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
        private String baseUrl = "https://api.cloudinary.com/v1_1";
        private String deliveryBaseUrl = "https://res.cloudinary.com";
        private String folder = "fittrack/meals";
        /** When true, assets are stored as authenticated and reachable only via signed URLs. */
        private boolean privateAssets = true;

        public String getCloudName() {
            return cloudName;
        }

        public void setCloudName(String cloudName) {
            this.cloudName = cloudName;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getDeliveryBaseUrl() {
            return deliveryBaseUrl;
        }

        public void setDeliveryBaseUrl(String deliveryBaseUrl) {
            this.deliveryBaseUrl = deliveryBaseUrl;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public boolean isPrivate() {
            return privateAssets;
        }

        public void setPrivate(boolean privateAssets) {
            this.privateAssets = privateAssets;
        }

        public boolean isConfigured() {
            return cloudName != null
                    && !cloudName.isBlank()
                    && apiKey != null
                    && !apiKey.isBlank()
                    && apiSecret != null
                    && !apiSecret.isBlank();
        }
    }
}
