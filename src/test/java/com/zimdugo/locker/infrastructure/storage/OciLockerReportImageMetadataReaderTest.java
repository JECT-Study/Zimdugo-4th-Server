package com.zimdugo.locker.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.bmc.model.BmcException;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.OciImagePathResolver;
import com.zimdugo.common.storage.OciObjectStorageClientProvider;
import com.zimdugo.common.storage.OciObjectStorageProperties;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import com.zimdugo.locker.domain.report.LockerReportImageMetadata;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class OciLockerReportImageMetadataReaderTest {

    @Mock
    private OciObjectStorageClientProvider clientProvider;

    @Mock
    private ObjectStorage objectStorage;

    @Mock
    private BmcException bmcException;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readsMetadataFromTheExactOciReportObjectAndClosesItsStream() throws Exception {
        CloseTrackingInputStream inputStream = new CloseTrackingInputStream(jpegWithExifMake());
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.getObject(any(GetObjectRequest.class)))
            .willReturn(response(inputStream, "image/jpeg"));

        LockerReportImageMetadata metadata = reader().readMetadata(publicUrl("reports/test.jpg"));

        assertThat(metadata.metadataJson()).isNotBlank();
        assertThat(metadata.extractedAt()).isNotNull();
        assertThat(hasTag(metadata.metadataJson(), "Make", "Codex")).isTrue();
        assertThat(inputStream.closed).isTrue();
        ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(objectStorage).getObject(captor.capture());
        assertThat(captor.getValue().getNamespaceName()).isEqualTo("testnamespace");
        assertThat(captor.getValue().getBucketName()).isEqualTo("test-bucket");
        assertThat(captor.getValue().getObjectName()).isEqualTo("reports/test.jpg");
    }

    @Test
    void readsGpsAndCapturedAtMetadata() {
        Metadata parsedMetadata = metadataWithGpsAndCapturedAt();
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.getObject(any(GetObjectRequest.class)))
            .willReturn(response(new byte[] {0x00}, "image/jpeg"));

        LockerReportImageMetadata metadata = parsingReader(parsedMetadata)
            .readMetadata(publicUrl("reports/test.jpg"));

        assertThat(metadata.gpsLatitude()).isCloseTo(37.556, offset(0.001));
        assertThat(metadata.gpsLongitude()).isCloseTo(126.923, offset(0.001));
        assertThat(metadata.gpsAltitude()).isCloseTo(123.4, offset(0.001));
        assertThat(metadata.capturedAt()).isNotNull();
    }

    @Test
    void rejectsExternalUrlBeforeCallingOci() {
        assertThatThrownBy(() -> reader().readMetadata("https://evil.example.com/reports/test.jpg"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_IMAGE_URL);

        verifyNoInteractions(clientProvider, objectStorage);
    }

    @Test
    void rejectsWrongBucketUrlBeforeCallingOci() {
        assertThatThrownBy(() -> reader().readMetadata(
            "https://objectstorage.ap-osaka-1.oraclecloud.com/n/testnamespace/b/other-bucket/o/reports/test.jpg"
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_IMAGE_URL);

        verifyNoInteractions(clientProvider, objectStorage);
    }

    @Test
    void rejectsNonReportImageUrlBeforeCallingOci() {
        assertThatThrownBy(() -> reader().readMetadata(publicUrl("profiles/1/test.jpg")))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_IMAGE_URL);

        verifyNoInteractions(clientProvider, objectStorage);
    }

    @Test
    void rejectsUnsupportedOciContentType() {
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.getObject(any(GetObjectRequest.class)))
            .willReturn(response(new byte[] {0x00}, "text/plain"));

        assertThatThrownBy(() -> reader().readMetadata(publicUrl("reports/test.jpg")))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void translatesMissingOciObjectToInvalidImageUrl() {
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.getObject(any(GetObjectRequest.class))).willThrow(bmcException);
        given(bmcException.getStatusCode()).willReturn(404);

        assertThatThrownBy(() -> reader().readMetadata(publicUrl("reports/missing.jpg")))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_IMAGE_URL);
    }

    @Test
    void sanitizesOciFailureBeforeLoggingOrPropagatingIt() {
        String endpointMarker = "https://objectstorage.ap-osaka-1.oraclecloud.com";
        String objectKeyMarker = "reports/private-token.jpg";
        String responseDetailMarker = "response-detail-marker";
        BmcException storageFailure = new BmcException(
            500,
            "InternalError",
            endpointMarker + "/n/testnamespace/b/test-bucket/o/" + objectKeyMarker
                + " " + responseDetailMarker,
            "safe-request-id"
        );
        given(clientProvider.get()).willReturn(objectStorage);
        given(objectStorage.getObject(any(GetObjectRequest.class))).willThrow(storageFailure);
        Logger logger = (Logger) LoggerFactory.getLogger(OciLockerReportImageMetadataReader.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            Throwable thrown = catchThrowable(() -> reader().readMetadata(publicUrl(objectKeyMarker)));

            assertThat(thrown)
                .isInstanceOf(ExternalApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.IMAGE_STORAGE_READ_FAILED);
            assertThat(thrown.getCause()).isNull();
            assertThat(thrown.getMessage())
                .doesNotContain(endpointMarker, objectKeyMarker, responseDetailMarker);
            assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .noneMatch(message -> containsAny(
                    message,
                    endpointMarker,
                    objectKeyMarker,
                    responseDetailMarker
                ));
            assertThat(appender.list)
                .noneMatch(event -> event.getThrowableProxy() != null);
        } finally {
            logger.detachAppender(appender);
        }
    }

    private OciLockerReportImageMetadataReader reader() {
        OciObjectStorageProperties properties = properties();
        return new OciLockerReportImageMetadataReader(
            clientProvider,
            objectMapper,
            properties,
            new ImageUploadPolicy(),
            new OciImagePathResolver(properties)
        );
    }

    private OciLockerReportImageMetadataReader parsingReader(Metadata metadata) {
        OciObjectStorageProperties properties = properties();
        return new OciLockerReportImageMetadataReader(
            clientProvider,
            objectMapper,
            properties,
            new ImageUploadPolicy(),
            new OciImagePathResolver(properties)
        ) {
            @Override
            protected Metadata parseMetadata(InputStream inputStream) {
                return metadata;
            }
        };
    }

    private OciObjectStorageProperties properties() {
        return new OciObjectStorageProperties(
            "ap-osaka-1",
            "testnamespace",
            "test-bucket",
            null,
            "DEFAULT",
            10,
            10_485_760
        );
    }

    private String publicUrl(String key) {
        return new OciImagePathResolver(properties()).buildPublicUrl(key);
    }

    private GetObjectResponse response(byte[] bytes, String contentType) {
        return response(new ByteArrayInputStream(bytes), contentType);
    }

    private GetObjectResponse response(InputStream inputStream, String contentType) {
        return GetObjectResponse.builder()
            .contentType(contentType)
            .inputStream(inputStream)
            .build();
    }

    private Metadata metadataWithGpsAndCapturedAt() {
        Metadata metadata = new Metadata();
        GpsDirectory gpsDirectory = new GpsDirectory();
        gpsDirectory.setObject(GpsDirectory.TAG_LATITUDE, new Rational[] {
            new Rational(37, 1), new Rational(33, 1), new Rational(21600, 1000)
        });
        gpsDirectory.setString(GpsDirectory.TAG_LATITUDE_REF, "N");
        gpsDirectory.setObject(GpsDirectory.TAG_LONGITUDE, new Rational[] {
            new Rational(126, 1), new Rational(55, 1), new Rational(22800, 1000)
        });
        gpsDirectory.setString(GpsDirectory.TAG_LONGITUDE_REF, "E");
        gpsDirectory.setObject(GpsDirectory.TAG_ALTITUDE, new Rational(1234, 10));
        gpsDirectory.setInt(GpsDirectory.TAG_ALTITUDE_REF, 0);
        metadata.addDirectory(gpsDirectory);

        ExifSubIFDDirectory subIfdDirectory = new ExifSubIFDDirectory();
        subIfdDirectory.setDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, new Date());
        metadata.addDirectory(subIfdDirectory);
        return metadata;
    }

    private boolean hasTag(String metadataJson, String tagName, String value) throws IOException {
        JsonNode root = objectMapper.readTree(metadataJson);
        Iterator<JsonNode> fields = root.elements();
        while (fields.hasNext()) {
            JsonNode node = fields.next();
            if (tagName.equals(node.path("tagName").asText()) && value.equals(node.path("value").asText())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAny(String value, String... markers) {
        for (String marker : markers) {
            if (value.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private byte[] jpegWithExifMake() {
        return new byte[] {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1, 0x00, 0x28,
            0x45, 0x78, 0x69, 0x66, 0x00, 0x00, 0x49, 0x49, 0x2A, 0x00,
            0x08, 0x00, 0x00, 0x00, 0x01, 0x00, 0x0F, 0x01, 0x02, 0x00,
            0x06, 0x00, 0x00, 0x00, 0x1A, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x43, 0x6F, 0x64, 0x65, 0x78, 0x00, (byte) 0xFF, (byte) 0xD9
        };
    }

    private static class CloseTrackingInputStream extends ByteArrayInputStream {

        private boolean closed;

        CloseTrackingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
