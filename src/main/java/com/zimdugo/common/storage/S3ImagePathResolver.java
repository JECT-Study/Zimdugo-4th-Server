package com.zimdugo.common.storage;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3ImagePathResolver {

    private static final String PROFILE_IMAGE_KEY_PREFIX = "profiles/";
    private static final String REPORT_IMAGE_KEY_PREFIX = "reports/";

    private final S3StorageProperties properties;

    public String createProfileImageKey(Long userId, String extension) {
        return PROFILE_IMAGE_KEY_PREFIX + userId + "/" + randomFileName(extension);
    }

    public String createReportImageKey(String extension) {
        return REPORT_IMAGE_KEY_PREFIX + randomFileName(extension);
    }

    public String createImageKey(String keyPrefix, String extension) {
        return keyPrefix + randomFileName(extension);
    }

    public String buildPublicUrl(String key) {
        return properties.normalizedPublicBaseUrl() + "/" + key;
    }

    public String resolveReportImageKey(String imageUrl) {
        String key = resolveKey(imageUrl);
        if (key.isBlank() || !key.startsWith(REPORT_IMAGE_KEY_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
        }
        return key;
    }

    public String resolveKey(String imageUrl) {
        String normalizedUrl = imageUrl.trim();
        String requiredPrefix = properties.normalizedPublicBaseUrl() + "/";
        if (!normalizedUrl.startsWith(requiredPrefix)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
        }

        return normalizedUrl.substring(requiredPrefix.length());
    }

    private String randomFileName(String extension) {
        return UUID.randomUUID() + "." + extension;
    }
}
