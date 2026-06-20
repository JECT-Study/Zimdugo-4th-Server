package com.zimdugo.admin.translation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "translation.gemini")
public record GeminiTranslationProperties(
    String baseUrl,
    String apiKey,
    String model,
    int connectTimeoutMillis,
    int readTimeoutMillis
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
