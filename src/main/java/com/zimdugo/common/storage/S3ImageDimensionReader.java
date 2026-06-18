package com.zimdugo.common.storage;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.heif.HeifDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ExternalApiException;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class S3ImageDimensionReader {

    private static final int HTTP_NOT_FOUND = 404;

    private final S3Client s3Client;
    private final S3StorageProperties properties;
    private final S3ImagePathResolver pathResolver;
    private final ImageUploadPolicy imageUploadPolicy;

    public int readWidth(String imageUrl) {
        String key = pathResolver.resolveKey(imageUrl);
        GetObjectRequest request = GetObjectRequest.builder()
            .bucket(properties.bucket())
            .key(key)
            .build();

        try (ResponseInputStream<GetObjectResponse> inputStream = s3Client.getObject(request)) {
            imageUploadPolicy.validateContentType(inputStream.response().contentType());
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            return extractWidth(metadata);
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL, e);
        } catch (S3Exception e) {
            if (e.statusCode() == HTTP_NOT_FOUND) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_URL, e);
            }
            log.warn("이미지 S3 조회 실패. imageUrl: {}", imageUrl, e);
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_READ_FAILED, e);
        } catch (SdkException e) {
            log.warn("이미지 S3 호출 실패. imageUrl: {}", imageUrl, e);
            throw new ExternalApiException(ErrorCode.IMAGE_STORAGE_READ_FAILED, e);
        } catch (ImageProcessingException | IOException e) {
            log.warn("이미지 크기 메타데이터 해석 실패. imageUrl: {}", imageUrl, e);
            throw new BusinessException(ErrorCode.IMAGE_METADATA_PARSE_FAILED, e);
        }
    }

    private int extractWidth(Metadata metadata) {
        Integer width = firstValidWidth(metadata);
        if (width == null) {
            throw new BusinessException(ErrorCode.IMAGE_METADATA_PARSE_FAILED);
        }
        return width;
    }

    private Integer firstValidWidth(Metadata metadata) {
        return java.util.stream.Stream.of(
                validWidth(jpegWidth(metadata)),
                validWidth(webpWidth(metadata)),
                validWidth(heifWidth(metadata)),
                validWidth(pngWidth(metadata))
            )
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private Integer jpegWidth(Metadata metadata) {
        return widthFrom(metadata.getFirstDirectoryOfType(JpegDirectory.class), JpegDirectory.TAG_IMAGE_WIDTH);
    }

    private Integer webpWidth(Metadata metadata) {
        return widthFrom(metadata.getFirstDirectoryOfType(WebpDirectory.class), WebpDirectory.TAG_IMAGE_WIDTH);
    }

    private Integer heifWidth(Metadata metadata) {
        return widthFrom(metadata.getFirstDirectoryOfType(HeifDirectory.class), HeifDirectory.TAG_IMAGE_WIDTH);
    }

    private Integer pngWidth(Metadata metadata) {
        return widthFrom(metadata.getFirstDirectoryOfType(PngDirectory.class), PngDirectory.TAG_IMAGE_WIDTH);
    }

    private Integer widthFrom(Directory directory, int tagType) {
        if (directory == null) {
            return null;
        }
        return directory.getInteger(tagType);
    }

    private Integer validWidth(Integer width) {
        if (width == null || width <= 0) {
            return null;
        }
        return width;
    }
}
