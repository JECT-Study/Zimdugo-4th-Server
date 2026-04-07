package com.zimdugo.auth.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

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
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
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

        response.setHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "oauth login success");
        body.put("userId", userId);
        body.put("email", email);
        body.put("accessToken", tokens.accessToken());

        log.info("oauth login success. userId={}, sid={}", userId, sid);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
