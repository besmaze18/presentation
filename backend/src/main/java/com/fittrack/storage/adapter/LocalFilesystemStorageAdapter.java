package com.fittrack.storage.adapter;

import com.fittrack.common.exception.ExternalServiceException;
import com.fittrack.storage.config.StorageProperties;
import com.fittrack.storage.service.StorageService;
import com.fittrack.storage.service.StorageUpload;
import com.fittrack.storage.service.StoredObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stores files on the local filesystem. Intended for development and single-node deployments; the
 * Docker Compose stack mounts a named volume so uploads survive a restart.
 *
 * <p>URLs are HMAC-signed and time-limited: an {@code <img>} tag cannot send an Authorization
 * header, so the signature is what makes a meal photo unguessable and short-lived rather than
 * simply public.
 */
@Component
public class LocalFilesystemStorageAdapter implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFilesystemStorageAdapter.class);
    private static final String PROVIDER = "local";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final StorageProperties properties;
    private final Path root;
    private final byte[] signingKey;
    private final Clock clock;

    public LocalFilesystemStorageAdapter(
            StorageProperties properties,
            @org.springframework.beans.factory.annotation.Value("${fittrack.crypto.encryption-key}")
                    String signingSecret,
            Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.root = Paths.get(properties.getLocal().getDirectory()).toAbsolutePath().normalize();
        this.signingKey = decodeKey(signingSecret);
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not create the local storage directory " + root, ex);
        }
    }

    private static byte[] decodeKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "fittrack.crypto.encryption-key must be set; it also signs local storage URLs");
        }
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ignored) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public String providerName() {
        return PROVIDER;
    }

    @Override
    public StoredObject upload(StorageUpload upload) {
        String key = buildKey(upload);
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, upload.content());
        } catch (IOException ex) {
            throw new ExternalServiceException(PROVIDER, "Could not write the uploaded file", ex);
        }
        log.debug("Stored object {} ({} bytes)", key, upload.content().length);
        return new StoredObject(
                PROVIDER, key, upload.contentType(), upload.content().length, Instant.now(clock));
    }

    @Override
    public Optional<byte[]> retrieve(String key) {
        Path path = resolve(key);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException ex) {
            throw new ExternalServiceException(PROVIDER, "Could not read stored file", ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ex) {
            throw new ExternalServiceException(PROVIDER, "Could not delete stored file", ex);
        }
    }

    @Override
    public String accessibleUrl(String key, Duration ttl) {
        long expiresAt = clock.instant().plus(ttl).getEpochSecond();
        String signature = sign(key, expiresAt);
        return properties.getLocal().getPublicBaseUrl()
                + "/" + key
                + "?expires=" + expiresAt
                + "&signature=" + signature;
    }

    /** Verifies a signed URL. Used by the controller that serves these files. */
    public boolean verifySignature(String key, long expiresAt, String signature) {
        if (clock.instant().getEpochSecond() > expiresAt) {
            return false;
        }
        String expected = sign(key, expiresAt);
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String key, long expiresAt) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(signingKey, HMAC_ALGORITHM));
            return HexFormat.of()
                    .formatHex(mac.doFinal((key + ":" + expiresAt).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException ex) {
            throw new IllegalStateException("Could not sign a storage URL", ex);
        }
    }

    private String buildKey(StorageUpload upload) {
        String folder = upload.folderHint() == null || upload.folderHint().isBlank()
                ? "uploads"
                : sanitiseSegment(upload.folderHint());
        return folder + "/" + UUID.randomUUID() + extensionFor(upload.contentType());
    }

    /**
     * Resolves a key under the storage root and refuses anything that escapes it, so a crafted key
     * cannot be used for path traversal.
     */
    private Path resolve(String key) {
        Path candidate = root.resolve(key).normalize();
        if (!candidate.startsWith(root)) {
            throw new ExternalServiceException(PROVIDER, "Rejected a storage key outside the root");
        }
        return candidate;
    }

    private static String sanitiseSegment(String value) {
        return value.replaceAll("[^A-Za-z0-9/_-]", "-");
    }

    static String extensionFor(String contentType) {
        if (contentType == null) {
            return "";
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
