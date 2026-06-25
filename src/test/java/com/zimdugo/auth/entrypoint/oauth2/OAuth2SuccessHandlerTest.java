package com.zimdugo.auth.entrypoint.oauth2;

import com.zimdugo.auth.application.OAuth2LoginSessionResult;
import com.zimdugo.auth.application.OAuth2LoginSessionService;
import com.zimdugo.auth.domain.SocialProviderTokenRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2SuccessHandlerTest {

    @Test
    void createsSessionOnSuccessfulAuthentication() throws Exception {
        OAuth2LoginSessionService sessionService = mock(OAuth2LoginSessionService.class);
        OAuth2CallbackUrlCookieManager callbackUrlCookieManager = mock(
            OAuth2CallbackUrlCookieManager.class
        );
        OAuth2AuthorizedClientService authorizedClientService = mock(OAuth2AuthorizedClientService.class);
        SocialProviderTokenRepository socialProviderTokenRepository = mock(SocialProviderTokenRepository.class);
        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(
            sessionService,
            callbackUrlCookieManager,
            authorizedClientService,
            socialProviderTokenRepository
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(callbackUrlCookieManager.resolveCallbackUrl(request)).thenReturn("zimdugo://login");
        when(sessionService.createSession(42L, "user@example.com", "USER"))
            .thenReturn(new OAuth2LoginSessionResult("refresh-token", Duration.ofDays(1), "sid"));
        DefaultOAuth2User principal = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            Map.of(
                "userId", 42L,
                "email", "user@example.com",
                "role", "USER"
            ),
            "email"
        );
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
            .clientId("client-id")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("https://api.zimdugo.com/login/oauth2/code/google")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
            .userNameAttributeName("sub")
            .clientName("Google")
            .build();
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
            clientRegistration,
            "user@example.com",
            new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "provider-at",
                Instant.now(),
                Instant.now().plusSeconds(300)
            ),
            new OAuth2RefreshToken("provider-rt", Instant.now())
        );
        when(authorizedClientService.loadAuthorizedClient("google", "user@example.com"))
            .thenReturn(authorizedClient);
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
            principal,
            principal.getAuthorities(),
            "google"
        );

        handler.onAuthenticationSuccess(
            request,
            response,
            authentication
        );

        verify(sessionService).createSession(42L, "user@example.com", "USER");
        verify(callbackUrlCookieManager).clearCallbackUrl(response);
        verify(socialProviderTokenRepository).save(
            any(),
            any(),
            any()
        );
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
            .anySatisfy(header -> assertThat(header).contains("refreshToken=refresh-token"));
        assertThat(response.getRedirectedUrl()).isEqualTo("zimdugo://login?code=LOGIN_SUCCESS");
    }
}
