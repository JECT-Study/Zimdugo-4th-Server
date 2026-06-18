package com.zimdugo.common.storage;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage.s3")
public record S3StorageProperties(
    @NotBlank
    String region,
    @NotBlank
    String bucket,
    @NotBlank
    @Pattern(regexp = "^https://.+$", message = "storage.s3.public-base-url은 https 절대 URL이어야 합니다.")
    String publicBaseUrl,
    @Min(1)
    @Max(60)
    long uploadExpirationMinutes,
    @Positive
    long maxUploadBytes
) {

    public String normalizedPublicBaseUrl() {
        String trimmed = publicBaseUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
