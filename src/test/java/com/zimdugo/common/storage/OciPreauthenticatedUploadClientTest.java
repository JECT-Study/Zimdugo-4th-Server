package com.zimdugo.common.storage;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.model.PreauthenticatedRequest;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OciPreauthenticatedUploadClientTest {

    @Mock
    private OciObjectStorageClientProvider clientProvider;

    @Mock
    private ObjectStorage objectStorage;

    @Mock
    private BmcException bmcException;

    @Test
    void createObjectWriteParForExactlyOneObject() {
        Instant now = Instant.parse("2026-08-03T00:00:00Z");
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.createPreauthenticatedRequest(any()))
            .willReturn(CreatePreauthenticatedRequestResponse.builder()
                .preauthenticatedRequest(PreauthenticatedRequest.builder()
                    .accessUri("/p/token/n/testnamespace/b/test-bucket/o/reports%2Ftest.jpg")
                    .build())
                .build());
        OciObjectStorageProperties properties = properties();
        OciPreauthenticatedUploadClient client = new OciPreauthenticatedUploadClient(
            clientProvider,
            properties,
            new OciImagePathResolver(properties),
            Clock.fixed(now, ZoneOffset.UTC)
        );

        PreauthenticatedUpload upload = client.createObjectWrite("reports/test.jpg");

        ArgumentCaptor<CreatePreauthenticatedRequestRequest> captor =
            ArgumentCaptor.forClass(CreatePreauthenticatedRequestRequest.class);
        verify(objectStorage).createPreauthenticatedRequest(captor.capture());
        CreatePreauthenticatedRequestDetails details =
            captor.getValue().getCreatePreauthenticatedRequestDetails();
        assertThat(details.getAccessType()).isEqualTo(
            CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite
        );
        assertThat(details.getObjectName()).isEqualTo("reports/test.jpg");
        assertThat(details.getTimeExpires()).isEqualTo(Date.from(now.plusSeconds(600)));
        assertThat(upload.uploadUrl()).startsWith(
            "https://objectstorage.ap-osaka-1.oraclecloud.com/p/token/"
        );
        assertThat(upload.fileUrl()).endsWith("/reports%2Ftest.jpg");
    }

    @Test
    void translateOciFailureWithoutLoggingPreauthenticatedUrl() {
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.createPreauthenticatedRequest(any())).willThrow(bmcException);
        OciObjectStorageProperties properties = properties();
        OciPreauthenticatedUploadClient client = new OciPreauthenticatedUploadClient(
            clientProvider,
            properties,
            new OciImagePathResolver(properties),
            Clock.systemUTC()
        );
        Logger logger = (Logger) LoggerFactory.getLogger(OciPreauthenticatedUploadClient.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            assertThat(catchThrowable(
                () -> client.createObjectWrite("reports/test.jpg")
            )).isInstanceOfSatisfying(ExternalApiException.class, exception -> {
                assertThat(exception.getErrorCode())
                    .isEqualTo(ErrorCode.IMAGE_STORAGE_WRITE_FAILED);
                assertThat(exception.getCause()).isSameAs(bmcException);
            });
            assertThat(appender.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.ERROR);
                assertThat(event.getFormattedMessage())
                    .contains("bucket=test-bucket")
                    .contains("key=reports/test.jpg")
                    .doesNotContain("/p/")
                    .doesNotContain("token");
                assertThat(event.getThrowableProxy()).isNull();
            });
        } finally {
            logger.detachAppender(appender);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidResponses")
    void rejectResponseWithoutPreauthenticatedAccessUri(
        String scenario,
        CreatePreauthenticatedRequestResponse response
    ) {
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.createPreauthenticatedRequest(any())).willReturn(response);
        OciObjectStorageProperties properties = properties();
        OciPreauthenticatedUploadClient client = new OciPreauthenticatedUploadClient(
            clientProvider,
            properties,
            new OciImagePathResolver(properties),
            Clock.systemUTC()
        );

        assertThat(catchThrowable(
            () -> client.createObjectWrite("reports/test.jpg")
        )).isInstanceOfSatisfying(ExternalApiException.class, exception ->
            assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.IMAGE_STORAGE_WRITE_FAILED)
        );
    }

    private static Stream<Arguments> invalidResponses() {
        return Stream.of(
            Arguments.of("null response", null),
            Arguments.of(
                "null preauthenticated request",
                CreatePreauthenticatedRequestResponse.builder().build()
            ),
            Arguments.of("null access URI", responseWithAccessUri(null)),
            Arguments.of("blank access URI", responseWithAccessUri("  "))
        );
    }

    private static CreatePreauthenticatedRequestResponse responseWithAccessUri(String accessUri) {
        return CreatePreauthenticatedRequestResponse.builder()
            .preauthenticatedRequest(PreauthenticatedRequest.builder()
                .accessUri(accessUri)
                .build())
            .build();
    }

    private OciObjectStorageProperties properties() {
        return new OciObjectStorageProperties(
            "ap-osaka-1",
            "testnamespace",
            "test-bucket",
            OciAuthenticationMode.CONFIG_FILE,
            "DEFAULT",
            10,
            10 * 1024 * 1024
        );
    }
}
