package com.zimdugo.image.application;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "storage.s3")
public record S3ImageProperties(
    @NotBlank
    String region,
    @NotBlank
    String bucket,
    @NotBlank
    String publicBaseUrl,
    @Min(1)
    long uploadExpirationMinutes
) {
}
