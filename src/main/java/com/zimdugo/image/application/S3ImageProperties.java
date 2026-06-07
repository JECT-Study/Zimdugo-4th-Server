package com.zimdugo.image.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.s3")
public record S3ImageProperties(
    String region,
    String bucket,
    String publicBaseUrl,
    long uploadExpirationMinutes
) {
}
