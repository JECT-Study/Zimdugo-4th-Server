package com.zimdugo.auth.entrypoint;

import com.zimdugo.auth.application.JwtTokenProvider;
import com.zimdugo.auth.domain.AuthTokens;
import com.zimdugo.auth.domain.RefreshTokenRepository;
import com.zimdugo.user.domain.User;
import com.zimdugo.user.infrastructure.UserJpaRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE = 60L * 60L * 24L * 30L;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String SAME_SITE_POLICY = "Lax";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserJpaRepository userJpaRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getValidatedRefreshToken(request);
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String sid = jwtTokenProvider.getSid(refreshToken);

        if (!refreshTokenRepository.matches(userId, sid, refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "refresh token mismatch"));
        }

        User user = getUser(userId);
        AuthTokens newTokens = reissueTokens(user, refreshToken);

        response.setHeader(
            HttpHeaders.SET_COOKIE,
            createRefreshTokenCookie(newTokens.refreshToken()).toString()
        );

        return ResponseEntity.ok(createRefreshResponse(user, newTokens));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        LogoutTarget logoutTarget = extractLogoutTarget(request);

        if (logoutTarget != null) {
            refreshTokenRepository.delete(logoutTarget.userId(), logoutTarget.sid());
        }

        response.setHeader(HttpHeaders.SET_COOKIE, createLogoutCookie().toString());
        return ResponseEntity.ok(Map.of("message", "logout success"));
    }

    private String getValidatedRefreshToken(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refresh token not found");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("invalid refresh token");
        }

        return refreshToken;
    }

    private User getUser(Long userId) {
        return userJpaRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("user not found. id=" + userId));
    }

    private AuthTokens reissueTokens(User user, String refreshToken) {
        String sid = jwtTokenProvider.getSid(refreshToken);
        long uv = jwtTokenProvider.getUv(refreshToken);

        AuthTokens newTokens = jwtTokenProvider.generateTokens(
            user.getId(),
            user.getEmail(),
            sid,
            uv
        );

        refreshTokenRepository.save(
            user.getId(),
            newTokens.sid(),
            newTokens.refreshJti(),
            newTokens.refreshToken(),
            Duration.ofSeconds(REFRESH_TOKEN_COOKIE_MAX_AGE)
        );

        return newTokens;
    }

    private Map<String, Object> createRefreshResponse(User user, AuthTokens newTokens) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "access token reissued");
        body.put("userId", user.getId());
        body.put("email", user.getEmail());
        body.put("accessToken", newTokens.accessToken());
        return body;
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(REFRESH_TOKEN_COOKIE_MAX_AGE)
            .sameSite(SAME_SITE_POLICY)
            .build();
    }

    private ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite(SAME_SITE_POLICY)
            .build();
    }

    private LogoutTarget extractLogoutTarget(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken != null && !refreshToken.isBlank() && jwtTokenProvider.validateToken(refreshToken)) {
            return new LogoutTarget(
                jwtTokenProvider.getUserId(refreshToken),
                jwtTokenProvider.getSid(refreshToken)
            );
        }

        String accessToken = extractAccessToken(request);

        if (accessToken != null && !accessToken.isBlank() && jwtTokenProvider.validateToken(accessToken)) {
            return new LogoutTarget(
                jwtTokenProvider.getUserId(accessToken),
                jwtTokenProvider.getSid(accessToken)
            );
        }

        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private String extractAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (!authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length());
    }

    private record LogoutTarget(Long userId, String sid) {
    }
}
