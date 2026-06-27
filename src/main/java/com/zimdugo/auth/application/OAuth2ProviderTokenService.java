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

        Instant accessTokenExpiresAt = authorizedClient.getAccessToken().getExpiresAt();
        AuthProvider authProvider = resolveAuthProvider(registrationId, userId);
        if (authProvider == null) {
            return;
        }

        socialProviderTokenRepository.save(
            userId,
            authProvider,
            new SocialProviderToken(
                authorizedClient.getAccessToken().getTokenValue(),
                accessTokenExpiresAt,
                extractRefreshToken(authorizedClient)
            )
        );
    }

    private AuthProvider resolveAuthProvider(String registrationId, Long userId) {
        try {
            return AuthProvider.valueOf(registrationId.toUpperCase());
        } catch (IllegalArgumentException exception) {
            log.warn("지원하지 않는 registrationId라 provider token 저장을 건너뜁니다. registrationId={}, userId={}",
                registrationId, userId);
            return null;
        }
    }

    private String extractRefreshToken(OAuth2AuthorizedClient authorizedClient) {
        if (authorizedClient.getRefreshToken() == null) {
            return null;
        }
        return authorizedClient.getRefreshToken().getTokenValue();
    }
}
