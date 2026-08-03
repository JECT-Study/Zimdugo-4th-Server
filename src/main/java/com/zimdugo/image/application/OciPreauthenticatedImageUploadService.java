package com.zimdugo.image.application;

import com.zimdugo.common.storage.ImageUploadPolicy;
import com.zimdugo.common.storage.OciImagePathResolver;
import com.zimdugo.common.storage.OciObjectStorageProperties;
import com.zimdugo.common.storage.OciPreauthenticatedUploadClient;
import com.zimdugo.common.storage.PreauthenticatedUpload;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OciPreauthenticatedImageUploadService implements PresignedImageUploadService {

    private final OciObjectStorageProperties properties;
    private final ImageUploadPolicy imageUploadPolicy;
    private final OciImagePathResolver pathResolver;
    private final OciPreauthenticatedUploadClient uploadClient;

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
        String key = switch (category) {
            case PROFILE -> pathResolver.createProfileImageKey(userId, extension);
            case LOCKER_REPORT -> pathResolver.createReportImageKey(extension);
        };
        PreauthenticatedUpload upload = uploadClient.createObjectWrite(key);
        log.info(
            "이미지 직접 업로드 URL 발급 완료. category={}, userId={}, key={}, contentType={}, contentLength={}",
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
}
