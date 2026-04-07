package com.zimdugo.auth.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpirationSeconds,
    long refreshTokenExpirationSeconds
) {
}
