package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.common.config.JwtProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

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

        // 세션 식별자
        String sid = UUID.randomUUID().toString();

        // 유저 버전 조회 (없으면 내부에서 초기화/기본값 처리)
        long uv = refreshTokenRepository.getUserVersion(userId);

        // AT + RT 발급
        AuthTokens tokens = jwtTokenProvider.generateTokens(userId, email, sid, uv);

        Duration rtTtl = Duration.ofSeconds(jwtProperties.refreshTokenExpirationSeconds());

        // 로그인 시점에는 RT 화이트리스트만 저장
        // jti 사용처리는 하지 않음
        refreshTokenRepository.save(userId, sid, tokens.refreshJti(), tokens.refreshToken(), rtTtl);

        // RT -> HttpOnly Cookie
        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
            .httpOnly(true)
            .secure(false) // 운영 true
            .path("/api/auth/refresh")
            .maxAge(rtTtl)
            .sameSite("Strict")
            .build();

        response.setHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // AT -> 응답 바디
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "oauth login success");
        body.put("userId", userId);
        body.put("email", email);
        body.put("accessToken", tokens.accessToken());

        log.info("oauth login success. userId={}, sid={}", userId, sid);

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
