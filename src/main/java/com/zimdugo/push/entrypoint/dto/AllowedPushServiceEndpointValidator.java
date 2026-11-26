package com.zimdugo.push.entrypoint.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 서버가 예약 시점에 직접 POST를 보내는 주소이므로, 브라우저 벤더의 웹 푸시 서비스 호스트만 허용한다.
 */
public class AllowedPushServiceEndpointValidator
    implements ConstraintValidator<AllowedPushServiceEndpoint, String> {

    private static final String HTTPS_SCHEME = "https";
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final int UNSPECIFIED_PORT = -1;
    private static final Set<String> ALLOWED_HOSTS = Set.of(
        "fcm.googleapis.com",
        "android.googleapis.com",
        "updates.push.services.mozilla.com",
        "web.push.apple.com"
    );
    private static final List<String> ALLOWED_HOST_SUFFIXES = List.of(
        ".notify.windows.com",
        ".push.apple.com"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            URI uri = new URI(value);
            return HTTPS_SCHEME.equalsIgnoreCase(uri.getScheme())
                && uri.getUserInfo() == null
                && isDefaultPort(uri.getPort())
                && isAllowedHost(uri.getHost());
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private boolean isDefaultPort(int port) {
        return port == UNSPECIFIED_PORT || port == DEFAULT_HTTPS_PORT;
    }

    private boolean isAllowedHost(String host) {
        if (host == null) {
            return false;
        }
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        return ALLOWED_HOSTS.contains(normalizedHost)
            || ALLOWED_HOST_SUFFIXES.stream().anyMatch(normalizedHost::endsWith);
    }
}
