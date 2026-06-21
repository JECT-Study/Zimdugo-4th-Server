package com.zimdugo.admin.application;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.S3StorageProperties;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminNoticeImageFileValidatorTest {

    @Test
    void acceptHighResolutionImageWithinPixelLimit() throws IOException {
        MockMultipartFile file = image("notice.png", "image/png", 2000, 2000);

        assertThatCode(() -> validator().validate(file)).doesNotThrowAnyException();
    }

    @Test
    void rejectImageExceedingPixelLimit() throws IOException {
        MockMultipartFile file = image("notice.png", "image/png", 1100, 1000);

        assertThatThrownBy(() -> validator(1_000_000).validate(file))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_IMAGE_DIMENSIONS)
            );
    }

    @Test
    void rejectUnreadableImageBytes() {
        MockMultipartFile file = new MockMultipartFile(
            "imageFiles",
            "notice.png",
            "image/png",
            new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> validator().validate(file))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.IMAGE_METADATA_PARSE_FAILED)
            );
    }

    @Test
    void rejectUnsupportedNoticeImageType() throws IOException {
        MockMultipartFile file = image("notice.gif", "image/gif", 1080, 200);

        assertThatThrownBy(() -> validator().validate(file))
            .isInstanceOfSatisfying(BusinessException.class, exception ->
                org.assertj.core.api.Assertions.assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.UNSUPPORTED_IMAGE_TYPE)
            );
    }

    private AdminNoticeImageFileValidator validator() {
        return validator(50_000_000);
    }

    private AdminNoticeImageFileValidator validator(long maxPixelCount) {
        return new AdminNoticeImageFileValidator(
            new AdminNoticeImageProperties(maxPixelCount),
            properties(),
            new ImageUploadPolicy()
        );
    }

    private S3StorageProperties properties() {
        return new S3StorageProperties(
            "ap-northeast-2",
            "bucket",
            "https://cdn.example.com",
            10,
            10 * 1024 * 1024
        );
    }

    private MockMultipartFile image(String fileName, String contentType, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        return new MockMultipartFile("imageFiles", fileName, contentType, output.toByteArray());
    }
}
