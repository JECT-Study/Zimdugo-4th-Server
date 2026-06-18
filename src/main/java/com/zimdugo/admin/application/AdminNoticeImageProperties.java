package com.zimdugo.admin.application;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "admin.notice-image")
public record AdminNoticeImageProperties(
    @Positive
    int requiredWidthPixels
) {
}
