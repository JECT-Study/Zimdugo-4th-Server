package com.zimdugo.image.application;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.PresignedUpload;
import com.zimdugo.common.storage.S3ImagePathResolver;
import com.zimdugo.common.storage.S3PresignedUploadClient;
import com.zimdugo.common.storage.S3StorageProperties;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PresignedImageUploadService implements PresignedImageUploadService {

    private final S3StorageProperties properties;
    private final ImageUploadPolicy imageUploadPolicy;
    private final S3ImagePathResolver pathResolver;
    private final S3PresignedUploadClient presignedUploadClient;

    @Override
    public PresignedUploadResult createPresignedUpload(
        UploadCategory category,
        String originalFileName,
        String contentType,
        Long contentLength,
        Long userId
    ) {
        validateRequest(category, originalFileName, contentLength, userId);

        String normalizedContentType = imageUploadPolicy.validateContentType(contentType);
        String extension = imageUploadPolicy.extractValidExtension(originalFileName);
        String key = createKey(category, extension, userId);
        PresignedUpload upload = presignedUploadClient.createPresignedPutObject(
            key,
            normalizedContentType,
            contentLength
        );
        log.info(
            "이미지 업로드 URL 발급 완료. category={}, userId={}, key={}, contentType={}, contentLength={}",
            category,
            userId,
            upload.key(),
            normalizedContentType,
            contentLength
        );

        return new PresignedUploadResult(
            upload.uploadUrl(),
            upload.fileUrl(),
            upload.key(),
            upload.expiresAt()
        );
    }

    private void validateRequest(
        UploadCategory category,
        String originalFileName,
        Long contentLength,
        Long userId
    ) {
        if (category == null || originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
        if (category == UploadCategory.PROFILE && userId == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
        if (contentLength == null || contentLength <= 0 || contentLength > properties.maxUploadBytes()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }

    private String createKey(UploadCategory category, String extension, Long userId) {
        return switch (category) {
            case PROFILE -> pathResolver.createProfileImageKey(userId, extension);
            case LOCKER_REPORT -> pathResolver.createReportImageKey(extension);
        };
    }
}
