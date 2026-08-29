package com.fittrack.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fittrack.common.exception.BadRequestException;
import com.fittrack.storage.config.StorageProperties;
import com.fittrack.storage.service.ImageValidator;
import com.fittrack.support.TestImages;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

class ImageValidatorTest {

    private final StorageProperties properties = new StorageProperties();
    private final ImageValidator validator = new ImageValidator(properties);

    @Test
    void acceptsARealPngAndReportsItsType() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "meal.png", MediaType.IMAGE_PNG_VALUE, TestImages.onePixelPng());

        assertThat(validator.validate(file)).isEqualTo("image/png");
    }

    @Test
    void rejectsAnEmptyUpload() {
        assertThatThrownBy(() -> validator.validate(
                        new MockMultipartFile("image", "a.png", MediaType.IMAGE_PNG_VALUE, new byte[0])))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("required");
    }

    @Test
    void rejectsAContentTypeWeDoNotAccept() {
        assertThatThrownBy(() -> validator.validate(new MockMultipartFile(
                        "image", "a.pdf", MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4".getBytes())))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported image type");
    }

    @Test
    void rejectsBytesThatDoNotMatchTheDeclaredType() {
        // A PNG renamed and re-declared as a JPEG.
        MockMultipartFile mismatched = new MockMultipartFile(
                "image", "meal.jpg", MediaType.IMAGE_JPEG_VALUE, TestImages.onePixelPng());

        assertThatThrownBy(() -> validator.validate(mismatched))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not match the declared type");
    }

    @Test
    void rejectsAnythingThatIsNotARecognisableImage() {
        assertThatThrownBy(() -> validator.validate(new MockMultipartFile(
                        "image", "a.png", MediaType.IMAGE_PNG_VALUE, "not an image at all".getBytes())))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a recognisable image");
    }

    @Test
    void enforcesTheConfiguredSizeLimit() {
        properties.setMaxFileSizeBytes(10);
        MockMultipartFile file = new MockMultipartFile(
                "image", "meal.png", MediaType.IMAGE_PNG_VALUE, TestImages.onePixelPng());

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("maximum size");
    }
}
