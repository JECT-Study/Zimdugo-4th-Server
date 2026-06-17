package com.zimdugo.locker.infrastructure;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import com.zimdugo.locker.domain.LockerReportImageMetadata;
import com.zimdugo.locker.domain.LockerReportImageMetadataReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Component
public class S3LockerReportImageMetadataReader implements LockerReportImageMetadataReader {

    private static final int HTTP_NOT_FOUND = 404;
    private static final String REPORT_IMAGE_KEY_PREFIX = "reports/";
    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/heic",
        "image/heif"
    );

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucket;
    private final String publicBaseUrl;

    public S3LockerReportImageMetadataReader(
        S3Client s3Client,
        ObjectMapper objectMapper,
        @Value("${storage.s3.bucket}") String bucket,
        @Value("${storage.s3.public-base-url}") String publicBaseUrl
    ) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucket = bucket;
        this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
    }

    @Override
    public LockerReportImageMetadata readMetadata(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return LockerReportImageMetadata.empty();
        }

        String key = resolveKey(imageUrl);
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request)) {
            validateContentType(inputStream.response().contentType());
            Metadata metadata = parseMetadata(inputStream);
            return buildImageMetadata(metadata);
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL, e);
        } catch (S3Exception e) {
            if (e.statusCode() == HTTP_NOT_FOUND) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_URL, e);
            }
            log.warn("제보 이미지 S3 조회 실패. imageUrl: {}", imageUrl, e);
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_READ_FAILED, e);
        } catch (SdkException e) {
            log.warn("제보 이미지 메타데이터 추출 중 S3 호출 실패. imageUrl: {}", imageUrl, e);
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_READ_FAILED, e);
        } catch (ImageProcessingException | IOException e) {
            log.warn("제보 이미지 메타데이터 해석 실패. imageUrl: {}", imageUrl, e);
            throw new BusinessException(ErrorCode.IMAGE_METADATA_PARSE_FAILED, e);
        }
    }

    private LockerReportImageMetadata buildImageMetadata(Metadata metadata) throws IOException {
        Double latitude = null;
        Double longitude = null;
        Double altitude = null;
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gpsDirectory != null) {
            GeoLocation geoLocation = gpsDirectory.getGeoLocation();
            if (geoLocation != null && !geoLocation.isZero()) {
                latitude = geoLocation.getLatitude();
                longitude = geoLocation.getLongitude();
            }
            altitude = extractAltitude(gpsDirectory);
        }

        LocalDateTime capturedAt = extractCapturedAt(metadata);

        return new LockerReportImageMetadata(
            objectMapper.writeValueAsString(toEntries(metadata)),
            LocalDateTime.now(),
            latitude,
            longitude,
            altitude,
            capturedAt
        );
    }

    protected Metadata parseMetadata(InputStream inputStream) throws ImageProcessingException, IOException {
        return ImageMetadataReader.readMetadata(inputStream);
    }

    private LocalDateTime extractCapturedAt(Metadata metadata) {
        ExifSubIFDDirectory subIFDDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (subIFDDirectory != null) {
            Date date = subIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (date != null) {
                return toLocalDateTime(date);
            }
        }

        ExifIFD0Directory ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (ifd0Directory != null) {
            Date date = ifd0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
            if (date != null) {
                return toLocalDateTime(date);
            }
        }
        return null;
    }

    private Double extractAltitude(GpsDirectory gpsDirectory) {
        Rational altitude = gpsDirectory.getRational(GpsDirectory.TAG_ALTITUDE);
        if (altitude == null) {
            return null;
        }

        double altitudeMeters = altitude.doubleValue();
        Integer altitudeRef = gpsDirectory.getInteger(GpsDirectory.TAG_ALTITUDE_REF);
        if (altitudeRef != null && altitudeRef == 1) {
            return -altitudeMeters;
        }
        return altitudeMeters;
    }

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
            .atZone(SEOUL_ZONE)
            .toLocalDateTime();
    }

    private String resolveKey(String imageUrl) {
        String normalizedUrl = imageUrl.trim();
        String requiredPrefix = publicBaseUrl + "/";
        if (!normalizedUrl.startsWith(requiredPrefix)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
        }

        String key = normalizedUrl.substring(requiredPrefix.length());
        if (key.isBlank() || !key.startsWith(REPORT_IMAGE_KEY_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
        }
        return key;
    }

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
        String normalizedContentType = contentType.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException(ErrorCode.IMAGE_STORAGE_CONFIGURATION_MISSING);
        }
        String trimmed = baseUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private List<MetadataEntry> toEntries(Metadata metadata) {
        List<MetadataEntry> entries = new ArrayList<>();
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                entries.add(new MetadataEntry(
                    directory.getName(),
                    tag.getTagName(),
                    tag.getTagType(),
                    tag.getDescription(),
                    stringifyValue(directory.getObject(tag.getTagType()))
                ));
            }
            for (String error : directory.getErrors()) {
                entries.add(new MetadataEntry(directory.getName(), "ERROR", null, error, error));
            }
        }
        return entries;
    }

    private Object stringifyValue(Object value) {
        if (value == null) {
            return null;
        }
        Class<?> valueClass = value.getClass();
        if (!valueClass.isArray()) {
            return value.toString();
        }

        int length = Array.getLength(value);
        List<String> values = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            Object item = Array.get(value, index);
            values.add(item == null ? null : item.toString());
        }
        return values;
    }

    private record MetadataEntry(
        String directory,
        String tagName,
        Integer tagType,
        String description,
        Object value
    ) {
    }
}
