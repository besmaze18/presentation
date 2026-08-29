package com.fittrack.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fittrack.common.exception.ExternalServiceException;
import com.fittrack.storage.adapter.LocalFilesystemStorageAdapter;
import com.fittrack.storage.config.StorageProperties;
import com.fittrack.storage.service.StorageUpload;
import com.fittrack.storage.service.StoredObject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFilesystemStorageAdapterTest {

    private static final String SIGNING_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    private static final Instant NOW = Instant.parse("2026-03-10T12:00:00Z");

    @TempDir
    Path tempDir;

    private LocalFilesystemStorageAdapter adapter;
    private Clock clock;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties();
        properties.getLocal().setDirectory(tempDir.toString());
        properties.getLocal().setPublicBaseUrl("http://localhost:8080/api/storage/files");
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        adapter = new LocalFilesystemStorageAdapter(properties, SIGNING_KEY, clock);
    }

    @Test
    void roundTripsAnUploadedFile() {
        byte[] content = "pretend-image-bytes".getBytes();
        StoredObject stored = adapter.upload(new StorageUpload(content, "image/png", "meal.png", "meals/u1"));

        assertThat(stored.provider()).isEqualTo("local");
        assertThat(stored.key()).startsWith("meals/u1/").endsWith(".png");
        assertThat(stored.sizeBytes()).isEqualTo(content.length);
        assertThat(adapter.retrieve(stored.key())).contains(content);
    }

    @Test
    void deleteRemovesTheFileAndIsIdempotent() {
        StoredObject stored =
                adapter.upload(new StorageUpload("x".getBytes(), "image/jpeg", "a.jpg", "meals/u1"));

        adapter.delete(stored.key());
        assertThat(adapter.retrieve(stored.key())).isEmpty();

        // Deleting again must not throw - callers clean up without checking first.
        adapter.delete(stored.key());
    }

    @Test
    void retrievingAnUnknownKeyReturnsEmptyRatherThanThrowing() {
        assertThat(adapter.retrieve("meals/u1/does-not-exist.png")).isEqualTo(Optional.empty());
    }

    @Test
    void signedUrlsCarryAnExpiryAndVerifyCorrectly() throws IOException {
        StoredObject stored =
                adapter.upload(new StorageUpload("x".getBytes(), "image/png", "a.png", "meals/u1"));

        String url = adapter.accessibleUrl(stored.key(), Duration.ofMinutes(15));
        Map<String, String> query = parseQuery(url);

        long expires = Long.parseLong(query.get("expires"));
        assertThat(expires).isEqualTo(NOW.plus(Duration.ofMinutes(15)).getEpochSecond());
        assertThat(adapter.verifySignature(stored.key(), expires, query.get("signature"))).isTrue();
    }

    @Test
    void rejectsATamperedSignatureAndAnExpiredUrl() {
        StoredObject stored =
                adapter.upload(new StorageUpload("x".getBytes(), "image/png", "a.png", "meals/u1"));
        String url = adapter.accessibleUrl(stored.key(), Duration.ofMinutes(15));
        Map<String, String> query = parseQuery(url);
        long expires = Long.parseLong(query.get("expires"));
        String signature = query.get("signature");

        assertThat(adapter.verifySignature(stored.key(), expires, "0".repeat(signature.length()))).isFalse();
        // A signature is bound to its key, so it cannot be replayed against another object.
        assertThat(adapter.verifySignature("meals/u1/other.png", expires, signature)).isFalse();
        // And it stops working once the expiry passes.
        assertThat(adapter.verifySignature(stored.key(), NOW.minusSeconds(1).getEpochSecond(), signature))
                .isFalse();
    }

    @Test
    void refusesAKeyThatWouldEscapeTheStorageRoot() {
        assertThatThrownBy(() -> adapter.retrieve("../../etc/passwd"))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("outside the root");
    }

    @Test
    void choosesTheFileExtensionFromTheContentType() {
        assertThat(adapter.upload(new StorageUpload("x".getBytes(), "image/jpeg", null, "m")).key())
                .endsWith(".jpg");
        assertThat(adapter.upload(new StorageUpload("x".getBytes(), "image/webp", null, "m")).key())
                .endsWith(".webp");
    }

    private static Map<String, String> parseQuery(String url) {
        String query = URI.create(url).getQuery();
        return java.util.Arrays.stream(query.split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(java.util.stream.Collectors.toMap(parts -> parts[0], parts -> parts[1]));
    }
}
