package com.zimdugo.admin.application;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.webp.WebpDirectory;
import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.S3StorageProperties;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class AdminNoticeImageFileValidator {

    private static final Set<String> NOTICE_IMAGE_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    private final AdminNoticeImageProperties noticeImageProperties;
    private final S3StorageProperties storageProperties;
    private final ImageUploadPolicy imageUploadPolicy;

    public void validate(MultipartFile file) {
        validateFileSize(file);
        String contentType = imageUploadPolicy.validateContentType(file.getContentType());
        if (!NOTICE_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
        imageUploadPolicy.extractValidExtension(file.getOriginalFilename());

        ImageDimensions dimensions = readDimensions(file);
        long pixelCount = (long) dimensions.width() * dimensions.height();
        if (pixelCount > noticeImageProperties.maxPixelCount()) {
            throw new BusinessException(
                ErrorCode.INVALID_IMAGE_DIMENSIONS,
                "공지 이미지 해상도는 총 " + noticeImageProperties.maxPixelCount() + "픽셀 이하여야 합니다."
            );
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > storageProperties.maxUploadBytes()) {
            throw new BusinessException(
                ErrorCode.INVALID_PARAMETER_FORMAT,
                "이미지 파일 크기는 0바이트보다 크고 " + storageProperties.maxUploadBytes() + "바이트 이하여야 합니다."
            );
        }
    }

    private ImageDimensions readDimensions(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
            ImageDimensions dimensions = firstValidDimensions(metadata);
            if (dimensions == null) {
                throw new BusinessException(ErrorCode.IMAGE_METADATA_PARSE_FAILED);
            }
            return dimensions;
        } catch (ImageProcessingException | IOException exception) {
            throw new BusinessException(ErrorCode.IMAGE_METADATA_PARSE_FAILED, exception);
        }
    }

    private ImageDimensions firstValidDimensions(Metadata metadata) {
        ImageDimensions jpeg = dimensionsFrom(
            metadata.getFirstDirectoryOfType(JpegDirectory.class),
            JpegDirectory.TAG_IMAGE_WIDTH,
            JpegDirectory.TAG_IMAGE_HEIGHT
        );
        if (jpeg != null) {
            return jpeg;
        }
        ImageDimensions png = dimensionsFrom(
            metadata.getFirstDirectoryOfType(PngDirectory.class),
            PngDirectory.TAG_IMAGE_WIDTH,
            PngDirectory.TAG_IMAGE_HEIGHT
        );
        if (png != null) {
            return png;
        }
        return dimensionsFrom(
            metadata.getFirstDirectoryOfType(WebpDirectory.class),
            WebpDirectory.TAG_IMAGE_WIDTH,
            WebpDirectory.TAG_IMAGE_HEIGHT
        );
    }

    private ImageDimensions dimensionsFrom(Directory directory, int widthTag, int heightTag) {
        if (directory == null) {
            return null;
        }
        Integer width = directory.getInteger(widthTag);
        Integer height = directory.getInteger(heightTag);
        if (width == null || height == null || width <= 0 || height <= 0) {
            return null;
        }
        return new ImageDimensions(width, height);
    }

    private record ImageDimensions(int width, int height) {
    }
}
