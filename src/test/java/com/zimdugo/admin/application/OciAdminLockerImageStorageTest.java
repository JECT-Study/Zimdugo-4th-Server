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
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OciAdminLockerImageStorageTest {

    private static final String IMAGE_PREFIX = "admin/locker-images/";

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
        doNothing().when(fileValidator).validate(first);
        doThrow(new BusinessException(ErrorCode.INVALID_IMAGE_DIMENSIONS))
            .when(fileValidator).validate(invalid);

        assertThatThrownBy(() -> storage().uploadAll(List.of(first, invalid)))
            .isInstanceOf(BusinessException.class);

        verifyNoInteractions(clientProvider, objectStorage);
    }

    @Test
    void putValidatedLockerImageIntoConfiguredOciObject() {
        MockMultipartFile image = file("locker.png");
        given(clientProvider.get()).willReturn(objectStorage);
        given(imageUploadPolicy.extractValidExtension("locker.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey(IMAGE_PREFIX, "png"))
            .willReturn(IMAGE_PREFIX + "generated.png");
        given(pathResolver.buildPublicUrl(IMAGE_PREFIX + "generated.png"))
            .willReturn(publicUrl(IMAGE_PREFIX + "generated.png"));

        List<String> urls = storage().uploadAll(List.of(image));

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(objectStorage).putObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getNamespaceName()).isEqualTo("testnamespace");
        assertThat(requestCaptor.getValue().getBucketName()).isEqualTo("test-bucket");
        assertThat(requestCaptor.getValue().getObjectName()).isEqualTo(IMAGE_PREFIX + "generated.png");
        assertThat(requestCaptor.getValue().getContentType()).isEqualTo("image/png");
        assertThat(requestCaptor.getValue().getContentLength()).isEqualTo(1L);
        assertThat(urls).containsExactly(publicUrl(IMAGE_PREFIX + "generated.png"));
    }

    @Test
    void deleteLockerImageFromConfiguredOciObject() {
        String key = IMAGE_PREFIX + "locker.png";
        given(pathResolver.resolveKey(publicUrl(key))).willReturn(key);
        given(clientProvider.get()).willReturn(objectStorage);

        storage().deleteAll(List.of(publicUrl(key)));

        ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(objectStorage).deleteObject(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getNamespaceName()).isEqualTo("testnamespace");
        assertThat(requestCaptor.getValue().getBucketName()).isEqualTo("test-bucket");
        assertThat(requestCaptor.getValue().getObjectName()).isEqualTo(key);
    }

    @Test
    void deletePreviouslyUploadedObjectWhenLaterUploadFails() {
        MockMultipartFile first = file("first.png");
        MockMultipartFile second = file("second.png");
        String firstKey = IMAGE_PREFIX + "first.png";
        BmcException failure = storageFailure();
        given(clientProvider.get()).willReturn(objectStorage);
        given(imageUploadPolicy.extractValidExtension("first.png")).willReturn("png");
        given(imageUploadPolicy.extractValidExtension("second.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey(IMAGE_PREFIX, "png"))
            .willReturn(firstKey, IMAGE_PREFIX + "second.png");
        given(pathResolver.buildPublicUrl(firstKey)).willReturn(publicUrl(firstKey));
        given(pathResolver.resolveKey(publicUrl(firstKey))).willReturn(firstKey);
        given(objectStorage.putObject(any(PutObjectRequest.class)))
            .willReturn(null)
            .willThrow(failure);

        assertThatThrownBy(() -> storage().uploadAll(List.of(first, second)))
            .isInstanceOfSatisfying(ExternalApiException.class, exception -> {
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
                assertThat(exception.getCause()).isNull();
            });

        InOrder calls = inOrder(objectStorage);
        calls.verify(objectStorage, times(2)).putObject(any(PutObjectRequest.class));
        calls.verify(objectStorage).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void rejectUnexpectedObjectPrefixWithoutDeletingIt() {
        String externalUrl = publicUrl("reports/private.png");
        given(pathResolver.resolveKey(externalUrl)).willReturn("reports/private.png");

        storage().deleteAll(List.of(externalUrl));

        verifyNoInteractions(clientProvider, objectStorage);
    }

    @Test
    void rejectNoticeImageWithoutDeletingIt() {
        String noticeKey = "admin/notice-images/notice.png";
        String noticeUrl = publicUrl(noticeKey);
        given(pathResolver.resolveKey(noticeUrl)).willReturn(noticeKey);

        storage().deleteAll(List.of(noticeUrl));

        verifyNoInteractions(clientProvider, objectStorage);
    }

    @Test
    void rejectInvalidUrlWithoutLoggingItsParToken() {
        String parUrl = "https://objectstorage.ap-osaka-1.oraclecloud.com/p/secret-token/"
            + "n/testnamespace/b/test-bucket/o/admin%2Flocker-images%2Flocker.png";
        given(pathResolver.resolveKey(parUrl))
            .willThrow(new BusinessException(ErrorCode.INVALID_IMAGE_URL));
        Logger logger = logger();
        ListAppender<ILoggingEvent> appender = attachAppender(logger);

        try {
            storage().deleteAll(List.of(parUrl));

            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.WARN);
                assertThat(event.getFormattedMessage())
                    .contains("보관함 이미지 OCI 삭제 실패")
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
    void sanitizeOciUploadFailureBeforeLoggingOrPropagatingIt() {
        MockMultipartFile image = file("locker.png");
        BmcException failure = storageFailure();
        given(clientProvider.get()).willReturn(objectStorage);
        given(imageUploadPolicy.extractValidExtension("locker.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey(IMAGE_PREFIX, "png"))
            .willReturn(IMAGE_PREFIX + "generated.png");
        given(objectStorage.putObject(any(PutObjectRequest.class))).willThrow(failure);
        Logger logger = logger();
        ListAppender<ILoggingEvent> appender = attachAppender(logger);

        try {
            Throwable thrown = catchThrowable(() -> storage().uploadAll(List.of(image)));

            assertThat(thrown)
                .isInstanceOf(ExternalApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
            assertThat(thrown.getCause()).isNull();
            assertNoSensitiveFailureDetail(thrown.getMessage(), appender);
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void sanitizeClientInitializationFailureBeforeLoggingOrPropagatingIt() {
        MockMultipartFile image = file("locker.png");
        IllegalStateException failure = new IllegalStateException(
            "private-key-path response-detail-marker"
        );
        given(imageUploadPolicy.extractValidExtension("locker.png")).willReturn("png");
        given(imageUploadPolicy.validateContentType("image/png")).willReturn("image/png");
        given(pathResolver.createImageKey(IMAGE_PREFIX, "png"))
            .willReturn(IMAGE_PREFIX + "generated.png");
        given(clientProvider.get()).willThrow(failure);
        Logger logger = logger();
        ListAppender<ILoggingEvent> appender = attachAppender(logger);

        try {
            Throwable thrown = catchThrowable(() -> storage().uploadAll(List.of(image)));

            assertThat(thrown)
                .isInstanceOf(ExternalApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
            assertThat(thrown.getCause()).isNull();
            assertNoSensitiveFailureDetail(thrown.getMessage(), appender);
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void sanitizeBestEffortOciDeleteFailure() {
        String key = IMAGE_PREFIX + "private-token.png";
        given(pathResolver.resolveKey(publicUrl(key))).willReturn(key);
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.deleteObject(any(DeleteObjectRequest.class)))
            .willThrow(storageFailure());
        Logger logger = logger();
        ListAppender<ILoggingEvent> appender = attachAppender(logger);

        try {
            storage().deleteAll(List.of(publicUrl(key)));

            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.WARN);
                assertThat(event.getFormattedMessage())
                    .contains("보관함 이미지 OCI 삭제 실패")
                    .doesNotContain("private-token", "response-detail-marker");
                assertThat(event.getThrowableProxy()).isNull();
            });
        } finally {
            logger.detachAppender(appender);
        }
    }

    private OciAdminLockerImageStorage storage() {
        return new OciAdminLockerImageStorage(
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
            "testnamespace",
            "test-bucket",
            null,
            "DEFAULT",
            10,
            10 * 1024 * 1024
        );
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile("imageFiles", name, "image/png", new byte[]{1});
    }

    private String publicUrl(String key) {
        return properties().publicBaseUrl() + "/" + key.replace("/", "%2F");
    }

    private BmcException storageFailure() {
        return new BmcException(
            500,
            "InternalError",
            "https://objectstorage.ap-osaka-1.oraclecloud.com/private-token response-detail-marker",
            "safe-request-id"
        );
    }

    private Logger logger() {
        return (Logger) LoggerFactory.getLogger(OciAdminLockerImageStorage.class);
    }

    private ListAppender<ILoggingEvent> attachAppender(Logger logger) {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private void assertNoSensitiveFailureDetail(
        String thrownMessage,
        ListAppender<ILoggingEvent> appender
    ) {
        assertThat(thrownMessage).doesNotContain("private-token", "response-detail-marker");
        assertThat(appender.list)
            .extracting(ILoggingEvent::getFormattedMessage)
            .noneMatch(message -> message.contains("private-token")
                || message.contains("response-detail-marker"));
        assertThat(appender.list).noneMatch(event -> event.getThrowableProxy() != null);
    }
}
