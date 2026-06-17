package com.zimdugo.locker.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.locker.domain.LockerReportImageMetadata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3LockerReportImageMetadataReaderTest {

    private static final String PUBLIC_BASE_URL = "https://cdn.example.com";

    @Mock
    private S3Client s3Client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void readMetadataFromReportImage() throws Exception {
        given(s3Client.getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
            .willReturn(responseInputStream(jpegWithExifMake()));
        S3LockerReportImageMetadataReader reader = new S3LockerReportImageMetadataReader(
            s3Client,
            objectMapper,
            "test-bucket",
            PUBLIC_BASE_URL
        );

        LockerReportImageMetadata metadata = reader.readMetadata(PUBLIC_BASE_URL + "/reports/test.jpg");

        assertThat(metadata.metadataJson()).isNotBlank();
        assertThat(metadata.extractedAt()).isNotNull();
        assertThat(hasTag(metadata.metadataJson(), "Make", "Codex")).isTrue();
    }

    @Test
    void readGpsAndCapturedAtMetadata() throws Exception {
        Metadata dummyMetadata = new Metadata();

        GpsDirectory gpsDir = new GpsDirectory();
        Rational[] latitudeRational = new Rational[] {
            new Rational(37, 1),
            new Rational(33, 1),
            new Rational(21600, 1000)
        };
        gpsDir.setObject(GpsDirectory.TAG_LATITUDE, latitudeRational);
        gpsDir.setString(GpsDirectory.TAG_LATITUDE_REF, "N");
        Rational[] longitudeRational = new Rational[] {
            new Rational(126, 1),
            new Rational(55, 1),
            new Rational(22800, 1000)
        };
        gpsDir.setObject(GpsDirectory.TAG_LONGITUDE, longitudeRational);
        gpsDir.setString(GpsDirectory.TAG_LONGITUDE_REF, "E");
        gpsDir.setObject(GpsDirectory.TAG_ALTITUDE, new Rational(1234, 10));
        gpsDir.setInt(GpsDirectory.TAG_ALTITUDE_REF, 0);
        dummyMetadata.addDirectory(gpsDir);

        ExifSubIFDDirectory subIfdDir = new ExifSubIFDDirectory();
        Date capturedDate = new Date();
        subIfdDir.setDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, capturedDate);
        dummyMetadata.addDirectory(subIfdDir);

        given(s3Client.getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
            .willReturn(responseInputStream(new byte[]{0x00}));

        S3LockerReportImageMetadataReader reader = new S3LockerReportImageMetadataReader(
            s3Client,
            objectMapper,
            "test-bucket",
            PUBLIC_BASE_URL
        ) {
            @Override
            protected Metadata parseMetadata(java.io.InputStream inputStream) {
                return dummyMetadata;
            }
        };

        LockerReportImageMetadata metadata = reader.readMetadata(PUBLIC_BASE_URL + "/reports/test.jpg");

        assertThat(metadata.gpsLatitude()).isCloseTo(37.556, org.assertj.core.data.Offset.offset(0.001));
        assertThat(metadata.gpsLongitude()).isCloseTo(126.923, org.assertj.core.data.Offset.offset(0.001));
        assertThat(metadata.gpsAltitude()).isCloseTo(123.4, org.assertj.core.data.Offset.offset(0.001));
        assertThat(metadata.capturedAt()).isNotNull();
    }

    @Test
    void rejectExternalImageUrl() {
        S3LockerReportImageMetadataReader reader = new S3LockerReportImageMetadataReader(
            s3Client,
            objectMapper,
            "test-bucket",
            PUBLIC_BASE_URL
        );

        assertThatThrownBy(() -> reader.readMetadata("https://evil.example.com/reports/test.jpg"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectNonReportImageUrl() {
        S3LockerReportImageMetadataReader reader = new S3LockerReportImageMetadataReader(
            s3Client,
            objectMapper,
            "test-bucket",
            PUBLIC_BASE_URL
        );

        assertThatThrownBy(() -> reader.readMetadata(PUBLIC_BASE_URL + "/profiles/1/test.jpg"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectUnsupportedS3ContentType() {
        given(s3Client.getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
            .willReturn(responseInputStream(new byte[]{0x00}, "text/plain"));
        S3LockerReportImageMetadataReader reader = new S3LockerReportImageMetadataReader(
            s3Client,
            objectMapper,
            "test-bucket",
            PUBLIC_BASE_URL
        );

        assertThatThrownBy(() -> reader.readMetadata(PUBLIC_BASE_URL + "/reports/test.jpg"))
            .isInstanceOf(BusinessException.class);
    }

    private ResponseInputStream<GetObjectResponse> responseInputStream(byte[] bytes) {
        return responseInputStream(bytes, "image/jpeg");
    }

    private ResponseInputStream<GetObjectResponse> responseInputStream(byte[] bytes, String contentType) {
        return new ResponseInputStream<>(
            GetObjectResponse.builder().contentType(contentType).build(),
            new ByteArrayInputStream(bytes)
        );
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

    private byte[] jpegWithExifMake() {
        return new byte[] {
            (byte) 0xFF, (byte) 0xD8,
            (byte) 0xFF, (byte) 0xE1,
            0x00, 0x28,
            0x45, 0x78, 0x69, 0x66, 0x00, 0x00,
            0x49, 0x49, 0x2A, 0x00,
            0x08, 0x00, 0x00, 0x00,
            0x01, 0x00,
            0x0F, 0x01,
            0x02, 0x00,
            0x06, 0x00, 0x00, 0x00,
            0x1A, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x43, 0x6F, 0x64, 0x65, 0x78, 0x00,
            (byte) 0xFF, (byte) 0xD9
        };
    }
}
