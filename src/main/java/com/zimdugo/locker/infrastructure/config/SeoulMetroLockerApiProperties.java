package com.zimdugo.locker.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "seoul-metro.locker-api")
public record SeoulMetroLockerApiProperties(
    @NotBlank String baseUrl,
    @NotBlank String apiKey,
    @Positive int connectTimeoutMillis,
    @Positive int readTimeoutMillis
) {
}
