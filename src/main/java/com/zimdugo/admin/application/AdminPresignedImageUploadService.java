package com.zimdugo.admin.application;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.PresignedUpload;
import com.zimdugo.common.storage.S3ImagePathResolver;
import com.zimdugo.common.storage.S3PresignedUploadClient;
import com.zimdugo.common.storage.S3StorageProperties;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminPresignedImageUploadService {

    private static final String NOTICE_IMAGE_KEY_PREFIX = "admin/notice-images/";

    private final S3StorageProperties properties;
    private final ImageUploadPolicy imageUploadPolicy;
    private final S3ImagePathResolver pathResolver;
    private final S3PresignedUploadClient presignedUploadClient;

    public PresignedUpload createPresignedUpload(
        AdminUploadCategory category,
        String originalFileName,
        String contentType,
        Long contentLength
    ) {
        validateRequest(category, originalFileName, contentLength);

        String normalizedContentType = imageUploadPolicy.validateContentType(contentType);
        String extension = imageUploadPolicy.extractValidExtension(originalFileName);
        String key = createKey(category, extension);

        return presignedUploadClient.createPresignedPutObject(
            key,
            normalizedContentType,
            contentLength
        );
    }

    private void validateRequest(AdminUploadCategory category, String originalFileName, Long contentLength) {
        if (category == null || originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
        if (contentLength == null || contentLength <= 0 || contentLength > properties.maxUploadBytes()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }

    private String createKey(AdminUploadCategory category, String extension) {
        return switch (category) {
            case NOTICE_IMAGE -> pathResolver.createImageKey(NOTICE_IMAGE_KEY_PREFIX, extension);
        };
    }
}
