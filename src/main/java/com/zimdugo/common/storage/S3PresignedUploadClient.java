package com.zimdugo.common.storage;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3PresignedUploadClient {

    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;
    private final S3ImagePathResolver pathResolver;

    public PresignedUpload createPresignedPutObject(
        String key,
        String contentType,
        Long contentLength
    ) {
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(properties.uploadExpirationMinutes()));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(properties.bucket())
            .key(key)
            .contentType(contentType)
            .contentLength(contentLength)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(properties.uploadExpirationMinutes()))
            .putObjectRequest(putObjectRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUpload(
            presignedRequest.url().toString(),
            pathResolver.buildPublicUrl(key),
            key,
            expiresAt
        );
    }
}
