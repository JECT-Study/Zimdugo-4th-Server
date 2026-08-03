package com.zimdugo.admin.application;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.OciImagePathResolver;
import com.zimdugo.common.storage.OciObjectStorageClientProvider;
import com.zimdugo.common.storage.OciObjectStorageProperties;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OciAdminNoticeImageStorageTest {

    @Mock
    private OciObjectStorageClientProvider clientProvider;

    @Mock
    private ObjectStorage objectStorage;

    @Mock
    private OciImagePathResolver pathResolver;

    @Mock
    private ImageUploadPolicy imageUploadPolicy;

    @Mock
    private AdminNoticeImageFileValidator fileValidator;

    @Test
    void validateEveryFileBeforeUploadingAnyFile() {
        MockMultipartFile first = file("first.png");
        MockMultipartFile invalid = file("invalid.png");
        doThrow(new BusinessException(ErrorCode.INVALID_IMAGE_DIMENSIONS))
            .when(fileValidator).validate(invalid);
        doNothing().when(fileValidator).validate(first);

        assertThatThrownBy(() -> storage().uploadAll(List.of(first, invalid)))
            .isInstanceOf(BusinessException.class);

        verifyNoInteractions(clientProvider, objectStorage);
    }

    @Test
    void putValidatedFileIntoConfiguredBucket() {
        MockMultipartFile image = file("notice.png");
        given(clientProvider.get()).willReturn(objectStorage);
        given(imageUploadPolicy.extractValidExtension("notice.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey("admin/notice-images/", "png"))
            .willReturn("admin/notice-images/generated.png");
        given(pathResolver.buildPublicUrl("admin/notice-images/generated.png"))
            .willReturn("https://objectstorage.ap-osaka-1.oraclecloud.com/n/ns/b/bucket/o/admin%2Fnotice-images%2Fgenerated.png");

        List<String> urls = storage().uploadAll(List.of(image));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(objectStorage).putObject(captor.capture());
        assertThat(captor.getValue().getNamespaceName()).isEqualTo("ns");
        assertThat(captor.getValue().getBucketName()).isEqualTo("bucket");
        assertThat(captor.getValue().getObjectName()).isEqualTo("admin/notice-images/generated.png");
        assertThat(captor.getValue().getContentType()).isEqualTo("image/png");
        assertThat(captor.getValue().getContentLength()).isEqualTo(1L);
        assertThat(urls).containsExactly(
            "https://objectstorage.ap-osaka-1.oraclecloud.com/n/ns/b/bucket/o/admin%2Fnotice-images%2Fgenerated.png"
        );
    }

    @Test
    void sanitizeBmcUploadFailureBeforeLoggingOrPropagatingIt() {
        MockMultipartFile image = file("notice.png");
        String endpointMarker = "https://objectstorage.ap-osaka-1.oraclecloud.com";
        String objectKeyMarker = "admin/notice-images/private-marker.png";
        String responseDetailMarker = "response-body-marker";
        BmcException storageFailure = new BmcException(
            503,
            "ServiceUnavailable",
            endpointMarker + "/n/ns/b/bucket/o/" + objectKeyMarker
                + " " + responseDetailMarker,
            "safe-request-id"
        );
        given(clientProvider.get()).willReturn(objectStorage);
        given(imageUploadPolicy.extractValidExtension("notice.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey("admin/notice-images/", "png"))
            .willReturn("admin/notice-images/generated.png");
        given(objectStorage.putObject(any(PutObjectRequest.class))).willThrow(storageFailure);
        Logger logger = (Logger) LoggerFactory.getLogger(OciAdminNoticeImageStorage.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            Throwable thrown = catchThrowable(() -> storage().uploadAll(List.of(image)));

            assertThat(thrown).isInstanceOfSatisfying(ExternalApiException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.IMAGE_STORAGE_WRITE_FAILED)
            );
            assertThat(thrown.getCause()).isNull();
            assertThat(thrown.getMessage())
                .doesNotContain(endpointMarker, objectKeyMarker, responseDetailMarker);

            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                assertThat(event.getFormattedMessage())
                    .contains("bucket=bucket")
                    .contains("key=admin/notice-images/generated.png")
                    .contains("contentType=image/png")
                    .contains("fileSize=1")
                    .contains("statusCode=503")
                    .contains("serviceCode=ServiceUnavailable")
                    .contains("requestId=safe-request-id")
                    .doesNotContain(endpointMarker, objectKeyMarker, responseDetailMarker);
                assertThat(event.getThrowableProxy()).isNull();
            });
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void sanitizeIoUploadFailureBeforeLoggingOrPropagatingIt() throws IOException {
        String ioMarker = "https://internal.example/private-body-marker";
        MultipartFile image = mock(MultipartFile.class);
        given(image.getOriginalFilename()).willReturn("notice.png");
        given(image.getContentType()).willReturn("image/png");
        given(image.getSize()).willReturn(1L);
        given(image.getInputStream()).willThrow(new IOException(ioMarker));
        given(imageUploadPolicy.extractValidExtension("notice.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey("admin/notice-images/", "png"))
            .willReturn("admin/notice-images/generated.png");
        Logger logger = (Logger) LoggerFactory.getLogger(OciAdminNoticeImageStorage.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            Throwable thrown = catchThrowable(() -> storage().uploadAll(List.of(image)));

            assertThat(thrown).isInstanceOf(ExternalApiException.class);
            assertThat(thrown.getCause()).isNull();
            assertThat(thrown.getMessage()).doesNotContain(ioMarker);
            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getFormattedMessage())
                    .contains("bucket=bucket")
                    .contains("key=admin/notice-images/generated.png")
                    .contains("contentType=image/png")
                    .contains("fileSize=1")
                    .contains("reason=IOException")
                    .doesNotContain(ioMarker);
                assertThat(event.getThrowableProxy()).isNull();
            });
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void deletePreviouslyUploadedObjectWhenLaterUploadFails() {
        MockMultipartFile first = file("first.png");
        MockMultipartFile second = file("second.png");
        String firstKey = "admin/notice-images/first.png";
        String firstUrl = "https://objectstorage.ap-osaka-1.oraclecloud.com/n/ns/b/bucket/o/admin%2Fnotice-images%2Ffirst.png";
        given(clientProvider.get()).willReturn(objectStorage);
        given(imageUploadPolicy.extractValidExtension("first.png")).willReturn("png");
        given(imageUploadPolicy.extractValidExtension("second.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey("admin/notice-images/", "png"))
            .willReturn(firstKey, "admin/notice-images/second.png");
        given(pathResolver.buildPublicUrl(firstKey)).willReturn(firstUrl);
        given(pathResolver.resolveKey(firstUrl)).willReturn(firstKey);
        given(objectStorage.putObject(any(PutObjectRequest.class)))
            .willReturn(null)
            .willThrow(bmcFailure());

        assertThatThrownBy(() -> storage().uploadAll(List.of(first, second)))
            .isInstanceOfSatisfying(ExternalApiException.class, exception ->
                assertThat(exception.getCause()).isNull()
            );

        ArgumentCaptor<DeleteObjectRequest> deleteCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(objectStorage).deleteObject(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue().getNamespaceName()).isEqualTo("ns");
        assertThat(deleteCaptor.getValue().getBucketName()).isEqualTo("bucket");
        assertThat(deleteCaptor.getValue().getObjectName()).isEqualTo(firstKey);
        InOrder calls = inOrder(objectStorage);
        calls.verify(objectStorage, times(2)).putObject(any(PutObjectRequest.class));
        calls.verify(objectStorage).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void rejectParUrlWithoutLoggingItsToken() {
        String parUrl = "https://objectstorage.ap-osaka-1.oraclecloud.com/p/secret-token/n/ns/b/bucket/o/admin%2Fnotice-images%2Fnotice.png";
        given(pathResolver.resolveKey(parUrl))
            .willThrow(new BusinessException(ErrorCode.INVALID_IMAGE_URL));
        Logger logger = (Logger) LoggerFactory.getLogger(OciAdminNoticeImageStorage.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            storage().deleteAll(List.of(parUrl));

            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.WARN);
                assertThat(event.getFormattedMessage())
                    .contains("공지 이미지 OCI 삭제 실패")
                    .doesNotContain("secret-token")
                    .doesNotContain("/p/");
                assertThat(event.getThrowableProxy()).isNull();
            });
            verifyNoInteractions(clientProvider, objectStorage);
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void rejectResolvedObjectOutsideNoticePrefixWithoutDeletingIt() {
        String publicUrl = "https://objectstorage.ap-osaka-1.oraclecloud.com/n/ns/b/bucket/o/reports%2Fother.png";
        given(pathResolver.resolveKey(publicUrl)).willReturn("reports/other.png");

        storage().deleteAll(List.of(publicUrl));

        verifyNoInteractions(clientProvider, objectStorage);
    }

    private OciAdminNoticeImageStorage storage() {
        return new OciAdminNoticeImageStorage(
            clientProvider,
            properties(),
            pathResolver,
            imageUploadPolicy,
            fileValidator
        );
    }

    private OciObjectStorageProperties properties() {
        return new OciObjectStorageProperties(
            "ap-osaka-1",
            "ns",
            "bucket",
            null,
            "DEFAULT",
            10,
            10 * 1024 * 1024
        );
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile("imageFiles", name, "image/png", new byte[]{1});
    }

    private BmcException bmcFailure() {
        return new BmcException(
            503,
            "ServiceUnavailable",
            "unsafe response detail",
            "safe-request-id"
        );
    }
}
