package com.zimdugo.auth.entrypoint;

import com.zimdugo.auth.config.AuthProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieFactory {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth/refresh";

    private final AuthProperties authProperties;

    public ResponseCookie create(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(authProperties.getCookie().getRefresh().isSecure())
            .path(REFRESH_TOKEN_COOKIE_PATH)
            .maxAge(maxAgeSeconds)
            .sameSite(authProperties.getCookie().getRefresh().getSameSite())
            .build();
    }

    public ResponseCookie create(String refreshToken, Duration maxAge) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(authProperties.getCookie().getRefresh().isSecure())
            .path(REFRESH_TOKEN_COOKIE_PATH)
            .maxAge(maxAge)
            .sameSite(authProperties.getCookie().getRefresh().getSameSite())
            .build();
    }

    public ResponseCookie clear() {
        return create("", 0);
    }
}
