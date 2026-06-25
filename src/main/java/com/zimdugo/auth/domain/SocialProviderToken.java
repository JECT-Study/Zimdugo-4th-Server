package com.zimdugo.auth.domain;

import java.time.Instant;

public record SocialProviderToken(
    String accessToken,
    Instant accessTokenExpiresAt,
    String refreshToken
) {
}
