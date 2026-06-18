package com.zimdugo.image.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.PresignedUpload;
import com.zimdugo.common.storage.S3ImagePathResolver;
import com.zimdugo.common.storage.S3PresignedUploadClient;
import com.zimdugo.common.storage.S3StorageProperties;
import com.zimdugo.core.exception.BusinessException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S3PresignedImageUploadServiceTest {

    @Mock
    private S3PresignedUploadClient presignedUploadClient;

    @Test
    void createPresignedUploadUsesReportImageKey() {
        given(presignedUploadClient.createPresignedPutObject(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq("image/jpeg"),
            org.mockito.ArgumentMatchers.eq(1024L)
        )).willReturn(new PresignedUpload(
            "https://s3.example.com/upload",
            "https://cdn.example.com/reports/test.jpg",
            "reports/test.jpg",
            Instant.parse("2026-06-18T00:00:00Z")
        ));
        S3PresignedImageUploadService service = new S3PresignedImageUploadService(
            properties(),
            new ImageUploadPolicy(),
            new S3ImagePathResolver(properties()),
            presignedUploadClient
        );

        PresignedUploadResult result = service.createPresignedUpload(
            UploadCategory.LOCKER_REPORT,
            "locker.jpg",
            "image/jpeg",
            1024L,
            1L
        );

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(presignedUploadClient).createPresignedPutObject(
            keyCaptor.capture(),
            org.mockito.ArgumentMatchers.eq("image/jpeg"),
            org.mockito.ArgumentMatchers.eq(1024L)
        );
        assertThat(keyCaptor.getValue()).startsWith("reports/");
        assertThat(result.fileUrl()).startsWith("https://cdn.example.com/reports/");
    }

    @Test
    void createPresignedUploadWithTooLargeContentLengthFails() {
        S3PresignedImageUploadService service = new S3PresignedImageUploadService(
            properties(),
            new ImageUploadPolicy(),
            new S3ImagePathResolver(properties()),
            presignedUploadClient
        );

        assertThatThrownBy(() -> service.createPresignedUpload(
            UploadCategory.LOCKER_REPORT,
            "locker.jpg",
            "image/jpeg",
            10_485_761L,
            1L
        )).isInstanceOf(BusinessException.class);
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
