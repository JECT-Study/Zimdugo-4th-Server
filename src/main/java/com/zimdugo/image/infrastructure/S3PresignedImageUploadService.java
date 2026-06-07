package com.zimdugo.image.infrastructure;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.image.application.PresignedImageUploadService;
import com.zimdugo.image.application.PresignedUploadResult;
import com.zimdugo.image.application.S3ImageProperties;
import com.zimdugo.image.application.UploadCategory;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3PresignedImageUploadService implements PresignedImageUploadService {

    private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/heic",
        "image/heif"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg",
        "jpeg",
        "png",
        "webp",
        "heic",
        "heif"
    );

    private final S3Presigner s3Presigner;
    private final S3ImageProperties properties;

    @Override
    public PresignedUploadResult createPresignedUpload(
        UploadCategory category,
        String originalFileName,
        String contentType,
        Long userId
    ) {
        validateConfiguration();
        validateRequest(category, originalFileName, contentType, userId);

        String extension = extractExtension(originalFileName);
        String key = createKey(category, extension, userId);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(properties.uploadExpirationMinutes()));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(properties.bucket())
            .key(key)
            .contentType(contentType)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(properties.uploadExpirationMinutes()))
            .putObjectRequest(putObjectRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUploadResult(
            presignedRequest.url().toString(),
            buildPublicUrl(key),
            key,
            expiresAt
        );
    }

    private void validateRequest(
        UploadCategory category,
        String originalFileName,
        String contentType,
        Long userId
    ) {
        if (category == null || originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
        if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
        if (category == UploadCategory.PROFILE && userId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }

        String extension = extractExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }

    private void validateConfiguration() {
        if (properties.bucket() == null || properties.bucket().isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 bucket is not configured.");
        }
        if (properties.publicBaseUrl() == null || properties.publicBaseUrl().isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 public base URL is not configured.");
        }
    }

    private String extractExtension(String originalFileName) {
        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == originalFileName.length() - 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }

        return originalFileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String createKey(UploadCategory category, String extension, Long userId) {
        String uuid = UUID.randomUUID().toString();

        return switch (category) {
            case PROFILE -> "profiles/" + userId + "/" + uuid + "." + extension;
            case LOCKER_REPORT -> "reports/" + uuid + "." + extension;
        };
    }

    private String buildPublicUrl(String key) {
        String baseUrl = properties.publicBaseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/" + key;
    }
}
