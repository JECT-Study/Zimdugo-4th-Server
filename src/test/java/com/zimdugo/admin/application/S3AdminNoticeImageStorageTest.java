package com.zimdugo.admin.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
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
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

    @Test
    void logUploadContextAndCauseWhenS3UploadFails() {
        MockMultipartFile image = file("notice.png");
        given(imageUploadPolicy.extractValidExtension("notice.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey("admin/notice-images/", "png"))
            .willReturn("admin/notice-images/generated.png");
        doThrow(SdkClientException.builder().message("credentials unavailable").build())
            .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        S3AdminNoticeImageStorage storage = new S3AdminNoticeImageStorage(
            s3Client,
            properties(),
            pathResolver,
            imageUploadPolicy,
            fileValidator
        );
        Logger logger = (Logger) LoggerFactory.getLogger(S3AdminNoticeImageStorage.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThatThrownBy(() -> storage.uploadAll(List.of(image)))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.IMAGE_STORAGE_WRITE_FAILED.getMessage());

            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                assertThat(event.getFormattedMessage())
                    .contains("bucket=bucket")
                    .contains("key=admin/notice-images/generated.png")
                    .contains("contentType=image/png")
                    .contains("fileSize=1");
                assertThat(event.getThrowableProxy().getClassName())
                    .isEqualTo(SdkClientException.class.getName());
            });
        } finally {
            logger.detachAppender(appender);
        }
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
