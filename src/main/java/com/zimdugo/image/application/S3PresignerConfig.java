package com.zimdugo.image.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3PresignerConfig {

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(S3ImageProperties properties) {
        try {
            return S3Presigner.builder()
                .region(Region.of(properties.region()))
                .build();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid AWS region configuration.", ex);
        }
    }
}
