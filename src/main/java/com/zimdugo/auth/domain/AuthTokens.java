package com.zimdugo.auth.domain;

public record AuthTokens(
        String accessToken,
        String refreshToken,
        String sid
) {}
