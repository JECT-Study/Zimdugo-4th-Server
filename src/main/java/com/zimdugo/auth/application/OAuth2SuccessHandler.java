package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final String SAME_SITE_POLICY = "Strict";
    private static final String REFRESH_COOKIE_PATH = "/api/auth/refresh";

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final OAuth2CallbackUrlCookieManager callbackUrlCookieManager;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        String callbackUrl = callbackUrlCookieManager.resolveCallbackUrl(request);

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        Long userId = Long.valueOf(attributes.get("userId").toString());
        String email = attributes.get("email") != null ? attributes.get("email").toString() : null;
        String role = attributes.get("role") != null ? attributes.get("role").toString() : "USER";
        String sid = UUID.randomUUID().toString();

        AuthTokens tokens = jwtTokenProvider.generateTokens(userId, email, role, sid);
        Duration rtTtl = Duration.ofSeconds(jwtProperties.refreshTokenExpirationSeconds());
        refreshTokenRepository.save(userId, sid, tokens.refreshToken(), rtTtl);

        ResponseCookie rtCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokens.refreshToken())
            .httpOnly(true)
            .secure(false)
            .path(REFRESH_COOKIE_PATH)
            .maxAge(rtTtl)
            .sameSite(SAME_SITE_POLICY)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());
        callbackUrlCookieManager.clearCallbackUrl(response);

        log.info("oauth login success. userId={}, sid={}, callbackUrl={}", userId, sid, callbackUrl);
        response.sendRedirect(appendCode(callbackUrl, "LOGIN_SUCCESS"));
    }

    private String appendCode(String callbackUrl, String code) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
            .replaceQueryParam("code", code)
            .build(true)
            .toUriString();
    }
}
