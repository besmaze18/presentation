package com.fittrack.storage.api;

import com.fittrack.common.exception.NotFoundException;
import com.fittrack.storage.adapter.LocalFilesystemStorageAdapter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Serves files held by the local storage adapter.
 *
 * <p>Authorisation here is the URL signature, not a bearer token: an {@code <img>} element cannot
 * attach an Authorization header. Signatures are HMAC-SHA256 over the key and expiry, so a URL is
 * unguessable and stops working once it expires.
 */
@RestController
@RequestMapping("/api/storage/files")
@Tag(name = "Storage", description = "Signed delivery of locally stored files")
public class StorageController {

    private final LocalFilesystemStorageAdapter localAdapter;

    public StorageController(LocalFilesystemStorageAdapter localAdapter) {
        this.localAdapter = localAdapter;
    }

    @GetMapping("/**")
    @Operation(summary = "Fetch a stored file using a signed, time-limited URL")
    public ResponseEntity<byte[]> serve(
            HttpServletRequest request,
            @RequestParam long expires,
            @RequestParam String signature) {

        String key = extractKey(request);
        if (key.isBlank() || !localAdapter.verifySignature(key, expires, signature)) {
            // Deliberately indistinguishable from a missing file: a caller learns nothing about
            // which keys exist by probing with a bad signature.
            throw new NotFoundException("File not found");
        }

        Optional<byte[]> content = localAdapter.retrieve(key);
        if (content.isEmpty()) {
            throw new NotFoundException("File not found");
        }

        return ResponseEntity.ok()
                .contentType(mediaTypeFor(key))
                .cacheControl(CacheControl.noStore())
                .body(content.get());
    }

    private static String extractKey(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (path == null) {
            path = request.getRequestURI();
        }
        int index = path.indexOf("/api/storage/files/");
        return index >= 0 ? path.substring(index + "/api/storage/files/".length()) : path;
    }

    private static MediaType mediaTypeFor(String key) {
        String lower = key.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MediaType.parseMediaType("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }
}
