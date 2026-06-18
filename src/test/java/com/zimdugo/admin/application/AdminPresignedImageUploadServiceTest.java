package com.zimdugo.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
class AdminPresignedImageUploadServiceTest {

    @Mock
    private S3PresignedUploadClient presignedUploadClient;

    @Test
    void createPresignedUploadUsesAdminNoticeImageKey() {
        given(presignedUploadClient.createPresignedPutObject(
            anyString(),
            eq("image/png"),
            eq(2048L)
        )).willReturn(new PresignedUpload(
            "https://s3.example.com/upload",
            "https://cdn.example.com/admin/notice-images/test.png",
            "admin/notice-images/test.png",
            Instant.parse("2026-06-18T00:00:00Z")
        ));
        AdminPresignedImageUploadService service = service();

        PresignedUpload upload = service.createPresignedUpload(
            AdminUploadCategory.NOTICE_IMAGE,
            "notice.png",
            "image/png",
            2048L
        );

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(presignedUploadClient).createPresignedPutObject(
            keyCaptor.capture(),
            eq("image/png"),
            eq(2048L)
        );
        assertThat(keyCaptor.getValue()).startsWith("admin/notice-images/");
        assertThat(upload.fileUrl()).startsWith("https://cdn.example.com/admin/notice-images/");
    }

    @Test
    void createPresignedUploadWithTooLargeContentLengthFails() {
        AdminPresignedImageUploadService service = service();

        assertThatThrownBy(() -> service.createPresignedUpload(
            AdminUploadCategory.NOTICE_IMAGE,
            "notice.png",
            "image/png",
            10_485_761L
        )).isInstanceOf(BusinessException.class);
    }

    private AdminPresignedImageUploadService service() {
        S3StorageProperties properties = properties();
        return new AdminPresignedImageUploadService(
            properties,
            new ImageUploadPolicy(),
            new S3ImagePathResolver(properties),
            presignedUploadClient
        );
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
