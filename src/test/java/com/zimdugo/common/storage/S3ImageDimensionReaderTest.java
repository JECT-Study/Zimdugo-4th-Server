package com.zimdugo.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3ImageDimensionReaderTest {

    @Mock
    private S3Client s3Client;

    @Test
    void readWidthFromS3Image() throws Exception {
        S3StorageProperties properties = properties();
        given(s3Client.getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
            .willReturn(responseInputStream(jpegImage(1080, 2400)));
        S3ImageDimensionReader reader = new S3ImageDimensionReader(
            s3Client,
            properties,
            new S3ImagePathResolver(properties),
            new ImageUploadPolicy()
        );

        int width = reader.readWidth("https://cdn.example.com/admin/notice-images/notice.jpg");

        assertThat(width).isEqualTo(1080);
    }

    private ResponseInputStream<GetObjectResponse> responseInputStream(byte[] bytes) {
        return new ResponseInputStream<>(
            GetObjectResponse.builder().contentType("image/jpeg").build(),
            new ByteArrayInputStream(bytes)
        );
    }

    private byte[] jpegImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.getGraphics().setColor(Color.WHITE);
        image.getGraphics().fillRect(0, 0, width, height);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);
        return outputStream.toByteArray();
    }

    private S3StorageProperties properties() {
        return new S3StorageProperties(
            "ap-northeast-2",
            "test-bucket",
            "https://cdn.example.com",
            10,
            10_485_760
        );
    }
}
