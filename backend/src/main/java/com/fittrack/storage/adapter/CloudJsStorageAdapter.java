package com.fittrack.storage.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fittrack.common.exception.ExternalServiceException;
import com.fittrack.storage.config.StorageProperties;
import com.fittrack.storage.service.StorageService;
import com.fittrack.storage.service.StorageUpload;
import com.fittrack.storage.service.StoredObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cloud.js object-storage adapter.
 *
 * <p><b>Provider note.</b> "Cloud.js" is not an unambiguous object-storage product. This adapter
 * implements the Cloudinary-compatible HTTP contract that Cloud.js exposes for image storage:
 * a signed multipart {@code POST {base}/{cloud_name}/image/upload}, a signed
 * {@code POST {base}/{cloud_name}/image/destroy}, and signed delivery URLs for private assets.
 * Requests are signed with SHA-1 over the sorted parameters plus the API secret, which is the
 * scheme that API documents.
 *
 * <p>If your deployment exposes a different contract, this class is the only thing that changes:
 * nothing outside {@code com.fittrack.storage.adapter} references Cloud.js concepts, and the
 * {@link StorageService} interface is what the rest of the application depends on. The unresolved
 * configuration is documented in docs/ARCHITECTURE.md.
 */
public class CloudJsStorageAdapter implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudJsStorageAdapter.class);
    private static final String PROVIDER = "cloudjs";
    /** Cloudinary-compatible signatures are SHA-1 over the sorted parameter string plus the secret. */
    private static final String SIGNATURE_ALGORITHM = "SHA-1";

    private final StorageProperties.CloudJs config;
    private final RestClient restClient;
    private final Clock clock;

    public CloudJsStorageAdapter(StorageProperties.CloudJs config, Clock clock) {
        this.config = config;
        this.clock = clock;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String providerName() {
        return PROVIDER;
    }

    @Override
    public StoredObject upload(StorageUpload upload) {
        long timestamp = clock.instant().getEpochSecond();
        String publicId = (upload.folderHint() == null ? "fittrack" : upload.folderHint())
                + "/" + UUID.randomUUID();

        // Only the parameters below are signed - the file part itself is not part of the signature.
        Map<String, String> signed = new TreeMap<>();
        signed.put("public_id", publicId);
        signed.put("timestamp", String.valueOf(timestamp));
        if (config.getFolder() != null && !config.getFolder().isBlank()) {
            signed.put("folder", config.getFolder());
        }
        if (config.isPrivate()) {
            signed.put("type", "authenticated");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        signed.forEach(body::add);
        body.add("api_key", config.getApiKey());
        body.add("signature", sign(signed));
        body.add("file", new NamedByteArrayResource(upload.content(), fileName(upload)));

        try {
            JsonNode response = restClient
                    .post()
                    .uri("/{cloudName}/image/upload", config.getCloudName())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.path("public_id").isMissingNode()) {
                throw new ExternalServiceException(PROVIDER, "Upload response did not contain a public id");
            }
            String storedKey = response.path("public_id").asText();
            log.debug("Uploaded object {} to Cloud.js", storedKey);
            return new StoredObject(
                    PROVIDER,
                    storedKey,
                    upload.contentType(),
                    response.path("bytes").asLong(upload.content().length),
                    Instant.ofEpochSecond(timestamp));
        } catch (RestClientException ex) {
            // The message deliberately omits request parameters so the API secret can never leak.
            throw new ExternalServiceException(PROVIDER, "Cloud.js upload failed", ex);
        }
    }

    @Override
    public Optional<byte[]> retrieve(String key) {
        try {
            byte[] content = RestClient.create()
                    .get()
                    .uri(accessibleUrl(key, Duration.ofMinutes(5)))
                    .retrieve()
                    .body(byte[].class);
            return Optional.ofNullable(content);
        } catch (RestClientException ex) {
            log.warn("Could not retrieve Cloud.js object {}", key);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        long timestamp = clock.instant().getEpochSecond();
        Map<String, String> signed = new TreeMap<>();
        signed.put("public_id", key);
        signed.put("timestamp", String.valueOf(timestamp));
        if (config.isPrivate()) {
            signed.put("type", "authenticated");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        signed.forEach(body::add);
        body.add("api_key", config.getApiKey());
        body.add("signature", sign(signed));

        try {
            restClient
                    .post()
                    .uri("/{cloudName}/image/destroy", config.getCloudName())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            throw new ExternalServiceException(PROVIDER, "Cloud.js delete failed", ex);
        }
    }

    @Override
    public String accessibleUrl(String key, Duration ttl) {
        if (!config.isPrivate()) {
            return config.getDeliveryBaseUrl() + "/" + config.getCloudName() + "/image/upload/" + key;
        }
        // Private assets are delivered through a time-limited signed URL.
        long expiresAt = clock.instant().plus(ttl).getEpochSecond();
        Map<String, String> signed = new LinkedHashMap<>();
        signed.put("expires_at", String.valueOf(expiresAt));
        signed.put("public_id", key);
        String signature = sign(new TreeMap<>(signed));
        return config.getDeliveryBaseUrl()
                + "/" + config.getCloudName()
                + "/image/authenticated/s--" + signature.substring(0, 8) + "--/"
                + key
                + "?expires_at=" + expiresAt
                + "&signature=" + signature;
    }

    /** SHA-1 of "k1=v1&k2=v2..." (keys sorted) with the API secret appended. */
    private String sign(Map<String, String> parameters) {
        StringBuilder builder = new StringBuilder();
        parameters.forEach((key, value) -> {
            if (!builder.isEmpty()) {
                builder.append('&');
            }
            builder.append(key).append('=').append(value);
        });
        builder.append(config.getApiSecret());
        try {
            MessageDigest digest = MessageDigest.getInstance(SIGNATURE_ALGORITHM);
            return HexFormat.of()
                    .formatHex(digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(SIGNATURE_ALGORITHM + " is unavailable", ex);
        }
    }

    private static String fileName(StorageUpload upload) {
        String original = upload.originalFilename();
        if (original == null || original.isBlank()) {
            return "upload" + LocalFilesystemStorageAdapter.extensionFor(upload.contentType());
        }
        return original.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /** ByteArrayResource that reports a filename, which multipart encoding requires. */
    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        private NamedByteArrayResource(byte[] content, String filename) {
            super(content);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
