package com.zimdugo.locker.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoLocalApiProperties.class)
public class KakaoLocalApiConfig {

    @Bean("kakaoLocalRestClient")
    public RestClient kakaoLocalRestClient(KakaoLocalApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMillis());
        requestFactory.setReadTimeout(properties.readTimeoutMillis());
        return RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory)
            .build();
    }
}
