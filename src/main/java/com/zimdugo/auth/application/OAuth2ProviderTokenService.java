package com.zimdugo.auth.application;

import com.zimdugo.auth.domain.SocialProviderToken;
import com.zimdugo.auth.domain.SocialProviderTokenRepository;
import com.zimdugo.user.domain.AuthProvider;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2ProviderTokenService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final SocialProviderTokenRepository socialProviderTokenRepository;

    public void saveProviderToken(Authentication authentication, Long userId) {
        if (!(authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken)) {
            return;
        }

        String registrationId = oauth2AuthenticationToken.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            registrationId,
            oauth2AuthenticationToken.getName()
        );
        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            log.warn("OAuth provider token 저장을 건너뜁니다. registrationId={}, userId={}", registrationId, userId);
            return;
        }

        String refreshToken = authorizedClient.getRefreshToken() == null
            ? null
            : authorizedClient.getRefreshToken().getTokenValue();
        Instant accessTokenExpiresAt = authorizedClient.getAccessToken().getExpiresAt();

        socialProviderTokenRepository.save(
            userId,
            AuthProvider.valueOf(registrationId.toUpperCase()),
            new SocialProviderToken(
                authorizedClient.getAccessToken().getTokenValue(),
                accessTokenExpiresAt,
                refreshToken
            )
        );
    }
}
