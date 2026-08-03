package com.zimdugo.locker.infrastructure.storage;

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
import com.zimdugo.locker.domain.report.LockerReportImageMetadataReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OciLockerReportImageMetadataReader implements LockerReportImageMetadataReader {

    private static final int HTTP_NOT_FOUND = 404;
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    private final OciObjectStorageClientProvider clientProvider;
    private final ObjectMapper objectMapper;
    private final OciObjectStorageProperties properties;
    private final ImageUploadPolicy imageUploadPolicy;
    private final OciImagePathResolver pathResolver;

    public OciLockerReportImageMetadataReader(
        OciObjectStorageClientProvider clientProvider,
        ObjectMapper objectMapper,
        OciObjectStorageProperties properties,
        ImageUploadPolicy imageUploadPolicy,
        OciImagePathResolver pathResolver
    ) {
        this.clientProvider = clientProvider;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.imageUploadPolicy = imageUploadPolicy;
        this.pathResolver = pathResolver;
    }

    @Override
    public LockerReportImageMetadata readMetadata(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return LockerReportImageMetadata.empty();
        }

        String key = pathResolver.resolveReportImageKey(imageUrl);
        GetObjectRequest request = GetObjectRequest.builder()
            .namespaceName(properties.namespace())
            .bucketName(properties.bucket())
            .objectName(key)
            .build();

        try {
            return readObjectMetadata(request);
        } catch (BmcException exception) {
            if (exception.getStatusCode() == HTTP_NOT_FOUND) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
            }
            log.warn(
                "제보 이미지 OCI 조회 실패. statusCode={}, serviceCode={}, requestId={}",
                exception.getStatusCode(),
                exception.getServiceCode(),
                exception.getOpcRequestId()
            );
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_READ_FAILED);
        } catch (ImageProcessingException | IOException exception) {
            log.warn("제보 이미지 메타데이터 해석 실패", exception);
            throw new BusinessException(ErrorCode.IMAGE_METADATA_PARSE_FAILED, exception);
        }
    }

    private LockerReportImageMetadata readObjectMetadata(GetObjectRequest request)
        throws ImageProcessingException, IOException {
        GetObjectResponse response = objectStorage().getObject(request);
        try (InputStream inputStream = response.getInputStream()) {
            imageUploadPolicy.validateContentType(response.getContentType());
            Metadata metadata = parseMetadata(inputStream);
            return buildImageMetadata(metadata);
        }
    }

    private ObjectStorage objectStorage() {
        try {
            return clientProvider.get();
        } catch (BmcException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn(
                "제보 이미지 OCI 클라이언트 초기화 실패. bucket={}, reason={}",
                properties.bucket(),
                exception.getClass().getSimpleName()
            );
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_READ_FAILED);
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
        ExifSubIFDDirectory subIfdDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (subIfdDirectory != null) {
            Date date = subIfdDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
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
        return altitudeRef != null && altitudeRef == 1 ? -altitudeMeters : altitudeMeters;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(SEOUL_ZONE).toLocalDateTime();
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
        if (!value.getClass().isArray()) {
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
