package com.zimdugo.push.entrypoint;

import com.zimdugo.push.config.PushDeviceProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushDeviceCookieFactory {

    public static final String DEVICE_TOKEN_COOKIE_NAME = "deviceToken";

    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(365);

    private final PushDeviceProperties pushDeviceProperties;

    public ResponseCookie create(String deviceToken) {
        // 기기 식별 토큰은 스크립트가 아닌 브라우저 요청에서만 전달되도록 하여 XSS 노출 범위를 줄인다.
        return ResponseCookie.from(DEVICE_TOKEN_COOKIE_NAME, deviceToken)
            .httpOnly(true)
            .secure(pushDeviceProperties.isCookieSecure())
            .sameSite("Lax")
            .path("/")
            .maxAge(COOKIE_MAX_AGE)
            .build();
    }
}
