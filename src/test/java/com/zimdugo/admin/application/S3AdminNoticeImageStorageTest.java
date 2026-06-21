package com.zimdugo.admin.application;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.S3ImagePathResolver;
import com.zimdugo.common.storage.S3StorageProperties;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class S3AdminNoticeImageStorageTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3ImagePathResolver pathResolver;

    @Mock
    private ImageUploadPolicy imageUploadPolicy;

    @Mock
    private AdminNoticeImageFileValidator fileValidator;

    @Test
    void validateEveryFileBeforeUploadingAnyFile() {
        MockMultipartFile first = file("first.png");
        MockMultipartFile invalid = file("invalid.png");
        doNothing().when(fileValidator).validate(first);
        doThrow(new BusinessException(ErrorCode.INVALID_IMAGE_DIMENSIONS))
            .when(fileValidator).validate(invalid);
        S3AdminNoticeImageStorage storage = new S3AdminNoticeImageStorage(
            s3Client,
            properties(),
            pathResolver,
            imageUploadPolicy,
            fileValidator
        );

        assertThatThrownBy(() -> storage.uploadAll(List.of(first, invalid)))
            .isInstanceOf(BusinessException.class);

        verifyNoInteractions(s3Client);
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile("imageFiles", name, "image/png", new byte[]{1});
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
}
