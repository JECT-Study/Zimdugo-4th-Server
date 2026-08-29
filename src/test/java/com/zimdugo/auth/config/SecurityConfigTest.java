package com.zimdugo.auth.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import com.zimdugo.auth.application.CustomOAuth2UserService;
import com.zimdugo.auth.entrypoint.filter.JwtAuthenticationFilter;
import com.zimdugo.auth.entrypoint.filter.OAuth2CallbackUrlCaptureFilter;
import com.zimdugo.auth.entrypoint.filter.PushOriginValidationFilter;
import com.zimdugo.auth.entrypoint.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.zimdugo.auth.entrypoint.oauth2.OAuth2FailureHandler;
import com.zimdugo.auth.entrypoint.oauth2.OAuth2SuccessHandler;
import com.zimdugo.common.security.CustomAccessDeniedHandler;
import com.zimdugo.common.security.CustomAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private HttpSecurity http;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private PushOriginValidationFilter pushOriginValidationFilter;

    @Mock
    private OAuth2CallbackUrlCaptureFilter oAuth2CallbackUrlCaptureFilter;

    @Mock
    private DefaultSecurityFilterChain securityFilterChain;

    @Mock
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;

    @Mock
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Mock
    private OAuth2FailureHandler oAuth2FailureHandler;

    @Mock
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Mock
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() throws Exception {
        given(http.securityMatcher(any(RequestMatcher.class))).willReturn(http);
        given(http.cors(any())).willReturn(http);
        given(http.csrf(any())).willReturn(http);
        given(http.headers(any())).willReturn(http);
        given(http.sessionManagement(any())).willReturn(http);
        given(http.exceptionHandling(any())).willReturn(http);
        given(http.authorizeHttpRequests(any())).willReturn(http);
        given(http.oauth2Login(any())).willReturn(http);
        given(http.logout(any())).willReturn(http);
        given(http.addFilterBefore(any(), any())).willReturn(http);
        given(http.build()).willReturn(securityFilterChain);
    }

    @Test
    void registersJwtFilterBeforeUsingItAsPushFilterAnchor() throws Exception {
        securityConfig.apiSecurityFilterChain(http);

        InOrder filterOrder = inOrder(http);
        filterOrder.verify(http)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        filterOrder.verify(http).addFilterBefore(pushOriginValidationFilter, JwtAuthenticationFilter.class);
    }
}
