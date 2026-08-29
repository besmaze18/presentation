package com.fittrack.storage.service;

import com.fittrack.common.exception.BadRequestException;
import com.fittrack.storage.config.StorageProperties;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validates uploaded images by inspecting the leading bytes rather than trusting the declared
 * content type or the file extension, either of which a client can set to anything.
 */
@Component
public class ImageValidator {

    public static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private static final Map<String, byte[]> MAGIC_NUMBERS = Map.of(
            "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png", new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A},
            "image/gif", new byte[] {'G', 'I', 'F', '8'});

    private final StorageProperties properties;

    public ImageValidator(StorageProperties properties) {
        this.properties = properties;
    }

    /** Returns the content type the bytes actually are. Throws when the file is not a valid image. */
    public String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("An image file is required");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new BadRequestException(
                    "Image exceeds the maximum size of " + properties.getMaxFileSizeBytes() + " bytes");
        }

        String declared = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (!ALLOWED_TYPES.contains(declared)) {
            throw new BadRequestException(
                    "Unsupported image type. Allowed types: " + String.join(", ", ALLOWED_TYPES));
        }

        byte[] header;
        try {
            byte[] content = file.getBytes();
            header = content.length >= 12 ? java.util.Arrays.copyOf(content, 12) : content;
        } catch (java.io.IOException ex) {
            throw new BadRequestException("The uploaded image could not be read");
        }

        String detected = detect(header);
        if (detected == null) {
            throw new BadRequestException("The uploaded file is not a recognisable image");
        }
        if (!detected.equals(declared)) {
            throw new BadRequestException(
                    "The file content does not match the declared type " + declared);
        }
        return detected;
    }

    private static String detect(byte[] header) {
        for (Map.Entry<String, byte[]> entry : MAGIC_NUMBERS.entrySet()) {
            if (startsWith(header, entry.getValue())) {
                return entry.getKey();
            }
        }
        // WebP: "RIFF" .... "WEBP"
        if (startsWith(header, new byte[] {'R', 'I', 'F', 'F'})
                && header.length >= 12
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
