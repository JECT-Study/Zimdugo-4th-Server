package com.zimdugo.auth.application;

import java.time.Duration;

public record OAuth2LoginSessionResult(
    String refreshToken,
    Duration refreshTokenTtl,
    String sid
) {
}
