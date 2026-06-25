package com.zimdugo.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zimdugo.auth.domain.SocialAccountUnlinkClient;
import com.zimdugo.auth.domain.SocialProviderToken;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class NaverSocialAccountUnlinkClient implements SocialAccountUnlinkClient {

    private static final long EXPIRY_BUFFER_SECONDS = 60L;

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public NaverSocialAccountUnlinkClient(
        RestClient.Builder restClientBuilder,
        @Value("${spring.security.oauth2.client.registration.naver.client-id}") String clientId,
        @Value("${spring.security.oauth2.client.registration.naver.client-secret}") String clientSecret
    ) {
        this.restClient = restClientBuilder.baseUrl("https://nid.naver.com").build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.NAVER;
    }

    @Override
    public void unlink(SocialAccount socialAccount, SocialProviderToken token) {
        try {
            restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/oauth2.0/token")
                    .queryParam("grant_type", "delete")
                    .queryParam("client_id", clientId)
                    .queryParam("client_secret", clientSecret)
                    .queryParam("access_token", resolveAccessToken(token))
                    .queryParam("service_provider", "NAVER")
                    .build())
                .retrieve()
                .body(NaverTokenResponse.class);
        } catch (RestClientException exception) {
            log.error("네이버 연동 해제에 실패했습니다. userId={}", socialAccount.getUser().getId(), exception);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, exception);
        }
    }

    private String resolveAccessToken(SocialProviderToken token) {
        if (isAccessTokenUsable(token)) {
            return token.accessToken();
        }
        if (token.refreshToken() == null || token.refreshToken().isBlank()) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        NaverTokenResponse response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/oauth2.0/token")
                .queryParam("grant_type", "refresh_token")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("refresh_token", token.refreshToken())
                .build())
            .retrieve()
            .body(NaverTokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
        return response.accessToken();
    }

    private boolean isAccessTokenUsable(SocialProviderToken token) {
        if (token.accessToken() == null || token.accessToken().isBlank()) {
            return false;
        }
        Instant expiresAt = token.accessTokenExpiresAt();
        return expiresAt == null || Instant.now().isBefore(expiresAt.minusSeconds(EXPIRY_BUFFER_SECONDS));
    }

    private record NaverTokenResponse(
        @JsonProperty("access_token") String accessToken
    ) {
    }
}
