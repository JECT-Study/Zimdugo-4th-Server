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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class KakaoSocialAccountUnlinkClient implements SocialAccountUnlinkClient {

    private static final long EXPIRY_BUFFER_SECONDS = 60L;

    private final RestClient apiRestClient;
    private final RestClient authRestClient;
    private final String clientId;
    private final String clientSecret;

    public KakaoSocialAccountUnlinkClient(
        RestClient.Builder restClientBuilder,
        @Value("${spring.security.oauth2.client.registration.kakao.client-id}") String clientId,
        @Value("${spring.security.oauth2.client.registration.kakao.client-secret}") String clientSecret
    ) {
        this.apiRestClient = restClientBuilder.baseUrl("https://kapi.kakao.com").build();
        this.authRestClient = restClientBuilder.baseUrl("https://kauth.kakao.com").build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public void unlink(SocialAccount socialAccount, SocialProviderToken token) {
        try {
            apiRestClient.post()
                .uri("/v1/user/unlink")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + resolveAccessToken(token))
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException exception) {
            log.error("카카오 연동 해제에 실패했습니다. userId={}", socialAccount.getUser().getId(), exception);
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

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", token.refreshToken());

        KakaoTokenResponse response = authRestClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(formData)
            .retrieve()
            .body(KakaoTokenResponse.class);

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

    private record KakaoTokenResponse(
        @JsonProperty("access_token") String accessToken
    ) {
    }
}
