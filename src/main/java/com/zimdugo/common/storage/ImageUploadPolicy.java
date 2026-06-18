package com.zimdugo.common.storage;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ImageUploadPolicy {

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

    public String normalizeContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        return contentType.trim().toLowerCase(Locale.ROOT);
    }

    public String validateContentType(String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        if (normalizedContentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }
        return normalizedContentType;
    }

    public String extractValidExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == originalFileName.length() - 1) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }

        String extension = originalFileName.substring(extensionIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
        return extension;
    }
}
