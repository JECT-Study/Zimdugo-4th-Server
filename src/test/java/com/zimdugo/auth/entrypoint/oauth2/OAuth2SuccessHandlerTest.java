package com.zimdugo.auth.entrypoint.oauth2;

import com.zimdugo.auth.application.OAuth2LoginSessionResult;
import com.zimdugo.auth.application.OAuth2LoginSessionService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private OAuth2LoginSessionService loginSessionService;

    @Mock
    private OAuth2CallbackUrlCookieManager callbackUrlCookieManager;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Test
    @DisplayName("userId가 없으면 로그인 실패로 리다이렉트하고 세션을 생성하지 않는다")
    void redirectsToLoginFailedWhenUserIdIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UsernamePasswordAuthenticationToken authentication = authentication(Map.of(
            "email", "test@zimdugo.com",
            "role", "USER"
        ));
        given(callbackUrlCookieManager.resolveCallbackUrl(request)).willReturn("https://zimdugo.com/login");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(loginSessionService, never()).createSession(any(), any(), any());
        verify(callbackUrlCookieManager).clearCallbackUrl(response);
        assertThat(response.getRedirectedUrl()).isEqualTo("https://zimdugo.com/login?code=LOGIN_FAILED");
    }

    @Test
    @DisplayName("userId가 있으면 로그인 세션을 생성하고 성공으로 리다이렉트한다")
    void createsSessionAndRedirectsToLoginSuccess() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UsernamePasswordAuthenticationToken authentication = authentication(Map.of(
            "userId", 1L,
            "email", "test@zimdugo.com",
            "role", "USER"
        ));
        given(callbackUrlCookieManager.resolveCallbackUrl(request)).willReturn("https://zimdugo.com/login");
        given(loginSessionService.createSession(1L, "test@zimdugo.com", "USER"))
            .willReturn(new OAuth2LoginSessionResult("refresh-token", Duration.ofDays(14), "sid-1"));

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(loginSessionService).createSession(1L, "test@zimdugo.com", "USER");
        verify(callbackUrlCookieManager).clearCallbackUrl(response);
        assertThat(response.getRedirectedUrl()).isEqualTo("https://zimdugo.com/login?code=LOGIN_SUCCESS");
    }

    private UsernamePasswordAuthenticationToken authentication(Map<String, Object> attributes) {
        DefaultOAuth2User principal = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            attributes.containsKey("userId") ? "userId" : "email"
        );
        return new UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.getAuthorities()
        );
    }
}
