package com.zimdugo.common.config;

import com.zimdugo.common.storage.S3StorageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3ClientConfig {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(S3StorageProperties properties) {
        return S3Client.builder()
            .region(Region.of(properties.region()))
            .build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(S3StorageProperties properties) {
        return S3Presigner.builder()
            .region(Region.of(properties.region()))
            .build();
    }
}
