package com.zimdugo.admin.translation;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(GeminiTranslationProperties.class)
public class GeminiTranslationConfig {

    @Bean
    public RestClient geminiRestClient(GeminiTranslationProperties properties) {
        return RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory(properties))
            .build();
    }

    private SimpleClientHttpRequestFactory requestFactory(GeminiTranslationProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(properties.connectTimeoutMillis()));
        factory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMillis()));
        return factory;
    }
}
