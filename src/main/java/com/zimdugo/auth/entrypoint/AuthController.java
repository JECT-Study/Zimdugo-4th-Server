package com.zimdugo.auth.entrypoint;

import com.zimdugo.auth.application.AccountWithdrawalService;
import com.zimdugo.auth.application.AuthCommandService;
import com.zimdugo.auth.application.AuthRefreshResult;
import com.zimdugo.core.response.RestResponse;
import com.zimdugo.core.response.SuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private static final long REFRESH_TOKEN_COOKIE_MAX_AGE = 60L * 60L * 24L * 30L;
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_HEADER_NAME = "X-Refresh-Token";
    private static final String SAME_SITE_POLICY = "Strict";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth/refresh";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthCommandService authCommandService;
    private final AccountWithdrawalService accountWithdrawalService;

    @PostMapping("/refresh")
    public ResponseEntity<RestResponse<Map<String, Object>>> refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie,
        @RequestHeader(name = REFRESH_TOKEN_HEADER_NAME, required = false) String refreshTokenHeader,
        HttpServletResponse response
    ) {
        AuthRefreshResult result = authCommandService.refresh(
            resolveRefreshToken(refreshTokenCookie, refreshTokenHeader)
        );

        response.setHeader(
            HttpHeaders.SET_COOKIE,
            createRefreshTokenCookie(result.refreshToken()).toString()
        );

        return ResponseEntity.ok(RestResponse.of(SuccessCode.OK, createRefreshResponse(result)));
    }

    @PostMapping("/logout")
    public ResponseEntity<RestResponse<Void>> logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshTokenCookie,
        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        HttpServletResponse response
    ) {
        authCommandService.logout(refreshTokenCookie, extractAccessToken(authorization));

        response.setHeader(HttpHeaders.SET_COOKIE, createLogoutCookie().toString());
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<RestResponse<Void>> withdraw(
        @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization,
        HttpServletResponse response
    ) {
        accountWithdrawalService.withdraw(extractAccessToken(authorization));
        response.setHeader(HttpHeaders.SET_COOKIE, createLogoutCookie().toString());
        return ResponseEntity.ok(RestResponse.ok(SuccessCode.OK));
    }

    private Map<String, Object> createRefreshResponse(AuthRefreshResult result) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "access token reissued");
        body.put("userId", result.userId());
        body.put("email", result.email());
        body.put("accessToken", result.accessToken());
        return body;
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(false)
            .path(REFRESH_TOKEN_COOKIE_PATH)
            .maxAge(REFRESH_TOKEN_COOKIE_MAX_AGE)
            .sameSite(SAME_SITE_POLICY)
            .build();
    }

    private ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(false)
            .path(REFRESH_TOKEN_COOKIE_PATH)
            .maxAge(0)
            .sameSite(SAME_SITE_POLICY)
            .build();
    }

    private String resolveRefreshToken(String refreshTokenCookie, String refreshTokenHeader) {
        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            return refreshTokenCookie;
        }
        return refreshTokenHeader;
    }

    private String extractAccessToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (!authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization.substring(BEARER_PREFIX.length());
    }
}
