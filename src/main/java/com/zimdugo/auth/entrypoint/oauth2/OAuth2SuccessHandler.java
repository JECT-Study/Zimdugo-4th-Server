package com.zimdugo.auth.entrypoint.oauth2;

import com.zimdugo.auth.application.OAuth2LoginSessionResult;
import com.zimdugo.auth.application.OAuth2LoginSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/api/auth/refresh";

    private final OAuth2LoginSessionService loginSessionService;
    private final OAuth2CallbackUrlCookieManager callbackUrlCookieManager;

    @Value("${auth.cookie.refresh.same-site:Strict}")
    private String refreshTokenCookieSameSite;

    @Value("${auth.cookie.refresh.secure:false}")
    private boolean refreshTokenCookieSecure;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        String callbackUrl = callbackUrlCookieManager.resolveCallbackUrl(request);

        DefaultOAuth2User oAuth2User = extractOAuth2User(authentication);
        if (oAuth2User == null) {
            handleInvalidUserInfo(response, callbackUrl, "OAuth2 principal이 DefaultOAuth2User가 아닙니다.");
            return;
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Long userId = extractUserId(attributes);
        if (userId == null) {
            handleInvalidUserInfo(response, callbackUrl, "OAuth2 사용자 식별값(userId)을 가져오지 못했습니다.");
            return;
        }

        String email = extractNullableAttribute(attributes, "email");
        String role = Objects.requireNonNullElse(extractNullableAttribute(attributes, "role"), "USER");

        OAuth2LoginSessionResult session = loginSessionService.createSession(userId, email, role);

        ResponseCookie rtCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, session.refreshToken())
            .httpOnly(true)
            .secure(refreshTokenCookieSecure)
            .path(REFRESH_COOKIE_PATH)
            .maxAge(session.refreshTokenTtl())
            .sameSite(refreshTokenCookieSameSite)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());
        callbackUrlCookieManager.clearCallbackUrl(response);

        log.info("OAuth 로그인 성공. userId={}, sid={}, callbackUrl={}", userId, session.sid(), callbackUrl);
        response.sendRedirect(appendCode(callbackUrl, "LOGIN_SUCCESS"));
    }

    private DefaultOAuth2User extractOAuth2User(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof DefaultOAuth2User oAuth2User)) {
            return null;
        }
        return oAuth2User;
    }

    private Long extractUserId(Map<String, Object> attributes) {
        if (attributes == null) {
            return null;
        }

        Object userIdValue = attributes.get("userId");
        if (userIdValue == null) {
            return null;
        }

        try {
            return Long.valueOf(userIdValue.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractNullableAttribute(Map<String, Object> attributes, String key) {
        if (attributes == null) {
            return null;
        }

        Object value = attributes.get(key);
        return value == null ? null : value.toString();
    }

    private void handleInvalidUserInfo(
        HttpServletResponse response,
        String callbackUrl,
        String reason
    ) throws IOException {
        callbackUrlCookieManager.clearCallbackUrl(response);
        log.warn("OAuth 로그인 성공 후 사용자 정보 검증에 실패했습니다. callbackUrl={}, 사유={}", callbackUrl, reason);
        response.sendRedirect(appendCode(callbackUrl, "LOGIN_FAILED"));
    }

    private String appendCode(String callbackUrl, String code) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
            .replaceQueryParam("code", code)
            .build(true)
            .toUriString();
    }
}
