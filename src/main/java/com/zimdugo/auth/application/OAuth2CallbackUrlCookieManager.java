package com.zimdugo.auth.application;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuth2CallbackUrlCookieManager {

    private static final String CALLBACK_URL_PARAM = "callbackUrl";
    private static final String CALLBACK_URL_COOKIE_NAME = "oauth2_callback_url";
    private static final int CALLBACK_URL_COOKIE_MAX_AGE_SECONDS = 300;
    private static final String SAME_SITE_POLICY = "Lax";
    private static final String RELATIVE_PATH_DEFAULT = "/";

    @Value("${auth.callback.frontend-base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${auth.callback.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOriginsProperty;

    private Set<String> allowedOrigins;

    @PostConstruct
    void initializeAllowedOrigins() {
        this.allowedOrigins = new LinkedHashSet<>();
        Arrays.stream(allowedOriginsProperty.split(","))
            .map(String::trim)
            .filter(v -> !v.isBlank())
            .map(this::extractOrigin)
            .forEach(allowedOrigins::add);

        String frontendOrigin = extractOrigin(frontendBaseUrl);
        if (frontendOrigin != null) {
            allowedOrigins.add(frontendOrigin);
        }
    }

    public void saveCallbackUrl(HttpServletRequest request, HttpServletResponse response) {
        String callbackUrl = normalize(request.getParameter(CALLBACK_URL_PARAM));
        addCookie(response, callbackUrl, CALLBACK_URL_COOKIE_MAX_AGE_SECONDS);
    }

    public String resolveCallbackUrl(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return toFrontendUrl(RELATIVE_PATH_DEFAULT);
        }

        for (Cookie cookie : cookies) {
            if (CALLBACK_URL_COOKIE_NAME.equals(cookie.getName())) {
                return normalize(decode(cookie.getValue()));
            }
        }

        return toFrontendUrl(RELATIVE_PATH_DEFAULT);
    }

    public void clearCallbackUrl(HttpServletResponse response) {
        addCookie(response, "", 0);
    }

    private void addCookie(HttpServletResponse response, String value, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(CALLBACK_URL_COOKIE_NAME, encode(value))
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(maxAgeSeconds)
            .sameSite(SAME_SITE_POLICY)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String normalize(String callbackUrl) {
        if (callbackUrl == null || callbackUrl.isBlank()) {
            return toFrontendUrl(RELATIVE_PATH_DEFAULT);
        }

        String trimmed = callbackUrl.trim();
        if (trimmed.contains("\r") || trimmed.contains("\n")) {
            log.warn("Unsafe callbackUrl detected. fallback to default. callbackUrl={}", trimmed);
            return toFrontendUrl(RELATIVE_PATH_DEFAULT);
        }

        if (trimmed.startsWith("/") && !trimmed.startsWith("//")) {
            return toFrontendUrl(trimmed);
        }

        String origin = extractOrigin(trimmed);
        if (origin == null || !allowedOrigins.contains(origin)) {
            log.warn("Unsafe callbackUrl detected. fallback to default. callbackUrl={}", trimmed);
            return toFrontendUrl(RELATIVE_PATH_DEFAULT);
        }

        return trimmed;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to decode callback cookie. fallback to default.", e);
            return toFrontendUrl(RELATIVE_PATH_DEFAULT);
        }
    }

    private String toFrontendUrl(String path) {
        String base = frontendBaseUrl;
        if (frontendBaseUrl.endsWith("/")) {
            base = frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1);
        }
        if (path == null || path.isBlank() || "/".equals(path)) {
            return base + "/";
        }
        return base + path;
    }

    private String extractOrigin(String url) {
        try {
            URI uri = new URI(url);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }

            String scheme = uri.getScheme().toLowerCase();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                return null;
            }

            if (uri.getPort() == -1) {
                return scheme + "://" + uri.getHost().toLowerCase();
            }
            return scheme + "://" + uri.getHost().toLowerCase() + ":" + uri.getPort();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
