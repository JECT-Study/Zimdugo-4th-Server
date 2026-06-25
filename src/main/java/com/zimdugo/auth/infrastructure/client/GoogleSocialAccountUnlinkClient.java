package com.zimdugo.auth.infrastructure.client;

import com.zimdugo.auth.domain.SocialAccountUnlinkClient;
import com.zimdugo.auth.domain.SocialProviderToken;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.user.domain.AuthProvider;
import com.zimdugo.user.domain.SocialAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class GoogleSocialAccountUnlinkClient implements SocialAccountUnlinkClient {

    private final RestClient restClient;

    public GoogleSocialAccountUnlinkClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://oauth2.googleapis.com").build();
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public void unlink(SocialAccount socialAccount, SocialProviderToken token) {
        try {
            restClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/revoke")
                    .queryParam("token", resolveRevocationToken(token))
                    .build())
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException exception) {
            log.error("구글 연동 해제에 실패했습니다. userId={}", socialAccount.getUser().getId(), exception);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, exception);
        }
    }

    private String resolveRevocationToken(SocialProviderToken token) {
        if (token.refreshToken() != null && !token.refreshToken().isBlank()) {
            return token.refreshToken();
        }
        if (token.accessToken() != null && !token.accessToken().isBlank()) {
            return token.accessToken();
        }
        throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
    }
}
