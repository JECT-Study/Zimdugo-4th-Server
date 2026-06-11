package com.zimdugo.locker.infrastructure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "kakao.local")
public record KakaoLocalApiProperties(
    @NotBlank
    String baseUrl,
    @NotBlank
    String restApiKey
) {
}
