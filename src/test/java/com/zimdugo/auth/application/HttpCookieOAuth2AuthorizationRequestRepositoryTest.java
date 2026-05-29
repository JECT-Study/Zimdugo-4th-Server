package com.zimdugo.auth.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.config.JacksonConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.assertThat;

class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    private static final String SIGNING_KEY = "test-signing-key";

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();
    private final HttpCookieOAuth2AuthorizationRequestRepository repository =
        new HttpCookieOAuth2AuthorizationRequestRepository(objectMapper, SIGNING_KEY);

    @Test
    @DisplayName("OAuth2 authorization request를 서명된 쿠키에 저장하고 다시 불러온다")
    void savesAndLoadsAuthorizationRequestFromCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://kauth.kakao.com/oauth/authorize")
            .clientId("client-id")
            .redirectUri("https://api.zimdugo.com/login/oauth2/code/kakao")
            .state("state-value")
            .authorizationRequestUri("https://kauth.kakao.com/oauth/authorize?client_id=client-id")
            .attributes(attrs -> attrs.put("registration_id", "kakao"))
            .build();

        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        Cookie cookie = response.getCookie("oauth2_auth_request");
        assertThat(cookie).isNotNull();

        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.setCookies(cookie);

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(callbackRequest);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getState()).isEqualTo("state-value");
        assertThat(loaded.getRedirectUri()).isEqualTo("https://api.zimdugo.com/login/oauth2/code/kakao");
        assertThat(loaded.getAttributes()).containsEntry("registration_id", "kakao");
    }

    @Test
    @DisplayName("서명이 변조된 쿠키는 무시한다")
    void returnsNullWhenCookieSignatureIsTampered() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("oauth2_auth_request", "tampered.payload"));

        OAuth2AuthorizationRequest loaded = repository.loadAuthorizationRequest(request);

        assertThat(loaded).isNull();
    }

    @Test
    @DisplayName("removeAuthorizationRequest는 쿠키를 비우고 기존 요청을 반환한다")
    void removesAuthorizationRequestCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        OAuth2AuthorizationRequest authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .clientId("client-id")
            .redirectUri("https://api.zimdugo.com/login/oauth2/code/google")
            .state("state-value")
            .authorizationRequestUri("https://accounts.google.com/o/oauth2/v2/auth?client_id=client-id")
            .attributes(attrs -> attrs.put("registration_id", "google"))
            .build();

        repository.saveAuthorizationRequest(authorizationRequest, request, response);

        MockHttpServletRequest callbackRequest = new MockHttpServletRequest();
        callbackRequest.setCookies(response.getCookie("oauth2_auth_request"));
        MockHttpServletResponse callbackResponse = new MockHttpServletResponse();

        OAuth2AuthorizationRequest removed = repository.removeAuthorizationRequest(callbackRequest, callbackResponse);

        assertThat(removed).isNotNull();
        assertThat(callbackResponse.getCookie("oauth2_auth_request")).isNotNull();
        assertThat(callbackResponse.getCookie("oauth2_auth_request").getMaxAge()).isZero();
    }
}
