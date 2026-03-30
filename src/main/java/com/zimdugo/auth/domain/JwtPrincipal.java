package com.zimdugo.auth.domain;

public record JwtPrincipal(
        Long userId,
        String email,
        String sid,
        String jti,
        long uv
) {}