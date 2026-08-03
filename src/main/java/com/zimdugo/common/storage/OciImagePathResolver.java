package com.zimdugo.common.storage;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component
@RequiredArgsConstructor
public class OciImagePathResolver {

    private static final String PROFILE_IMAGE_KEY_PREFIX = "profiles/";
    private static final String REPORT_IMAGE_KEY_PREFIX = "reports/";

    private final OciObjectStorageProperties properties;

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
        return properties.publicBaseUrl() + "/" + UriUtils.encodePathSegment(key, StandardCharsets.UTF_8);
    }

    public String resolveReportImageKey(String imageUrl) {
        String key = resolveKey(imageUrl);
        if (!key.startsWith(REPORT_IMAGE_KEY_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
        }
        return key;
    }

    public String resolveKey(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl.trim());
            URI endpoint = URI.create(properties.objectStorageEndpoint());
            String requiredRawPath = URI.create(properties.publicBaseUrl()).getRawPath() + "/";
            String rawPath = uri.getRawPath();

            if (!"https".equalsIgnoreCase(uri.getScheme())
                || !endpoint.getHost().equalsIgnoreCase(uri.getHost())
                || uri.getRawUserInfo() != null
                || uri.getPort() != -1
                || uri.getRawQuery() != null
                || uri.getRawFragment() != null
                || rawPath == null
                || !rawPath.startsWith(requiredRawPath)) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
            }

            String key = UriUtils.decode(
                rawPath.substring(requiredRawPath.length()),
                StandardCharsets.UTF_8
            );
            if (key.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_URL);
            }
            return key;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_URL, exception);
        }
    }

    private String randomFileName(String extension) {
        return UUID.randomUUID() + "." + extension;
    }
}
