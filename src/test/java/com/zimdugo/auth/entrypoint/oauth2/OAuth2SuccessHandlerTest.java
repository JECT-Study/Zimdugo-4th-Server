package com.zimdugo.auth.entrypoint.oauth2;

import com.zimdugo.auth.application.OAuth2LoginSessionResult;
import com.zimdugo.auth.application.OAuth2LoginSessionService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
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
        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(
            sessionService,
            callbackUrlCookieManager
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

        handler.onAuthenticationSuccess(
            request,
            response,
            new TestingAuthenticationToken(principal, null)
        );

        verify(sessionService).createSession(42L, "user@example.com", "USER");
        verify(callbackUrlCookieManager).clearCallbackUrl(response);
        assertThat(response.getHeaders(HttpHeaders.SET_COOKIE))
            .anySatisfy(header -> assertThat(header).contains("refreshToken=refresh-token"));
        assertThat(response.getRedirectedUrl()).isEqualTo("zimdugo://login?code=LOGIN_SUCCESS");
    }
}
