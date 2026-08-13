package com.zimdugo.locker.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.locker.infrastructure.realtime.SeoulMetroLockerAvailabilityClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SeoulMetroLockerApiProperties.class)
public class SeoulMetroLockerApiConfig {

    @Bean
    public SeoulMetroLockerAvailabilityClient seoulMetroLockerAvailabilityClient(
        SeoulMetroLockerApiProperties properties,
        ObjectMapper objectMapper
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMillis());
        requestFactory.setReadTimeout(properties.readTimeoutMillis());
        RestClient restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(requestFactory)
            .build();
        return new SeoulMetroLockerAvailabilityClient(restClient, objectMapper, properties.apiKey());
    }
}
