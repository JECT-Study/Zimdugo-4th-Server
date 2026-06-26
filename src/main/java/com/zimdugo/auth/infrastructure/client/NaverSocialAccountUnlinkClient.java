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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
            restClient.post()
                .uri("/oauth2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(deleteFormData(token))
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

        NaverTokenResponse response = restClient.post()
            .uri("/oauth2.0/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(refreshFormData(token))
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

    private MultiValueMap<String, String> deleteFormData(SocialProviderToken token) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "delete");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("access_token", resolveAccessToken(token));
        formData.add("service_provider", "NAVER");
        return formData;
    }

    private MultiValueMap<String, String> refreshFormData(SocialProviderToken token) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", token.refreshToken());
        return formData;
    }

    private record NaverTokenResponse(
        @JsonProperty("access_token") String accessToken
    ) {
    }
}
