package com.zimdugo.auth.application;

public record AuthRefreshResult(
    Long userId,
    String email,
    String accessToken,
    String refreshToken
) {
}
