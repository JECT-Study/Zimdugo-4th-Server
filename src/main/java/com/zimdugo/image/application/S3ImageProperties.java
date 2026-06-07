package com.zimdugo.image.application;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^https://.+$", message = "storage.s3.public-base-url must be an absolute https URL.")
    String publicBaseUrl,
    @Min(1)
    @Max(60)
    long uploadExpirationMinutes
) {
}
