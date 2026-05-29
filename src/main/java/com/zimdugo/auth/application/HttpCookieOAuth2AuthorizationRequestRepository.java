package com.zimdugo.auth.application;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int AUTH_REQUEST_COOKIE_MAX_AGE_SECONDS = 300;
    private static final String SAME_SITE_POLICY = "Lax";

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (AUTH_REQUEST_COOKIE_NAME.equals(cookie.getName())) {
                return deserialize(cookie.getValue());
            }
        }

        return null;
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }

        addCookie(response, serialize(authorizationRequest), AUTH_REQUEST_COOKIE_MAX_AGE_SECONDS, request.isSecure());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        addCookie(response, "", 0, request.isSecure());
        return authorizationRequest;
    }

    private void addCookie(HttpServletResponse response, String value, int maxAgeSeconds, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(AUTH_REQUEST_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(maxAgeSeconds)
            .sameSite(SAME_SITE_POLICY)
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        byte[] bytes = SerializationUtils.serialize(authorizationRequest);
        if (bytes == null) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest");
        }
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value);
            Object deserialized = SerializationUtils.deserialize(bytes);
            if (deserialized instanceof OAuth2AuthorizationRequest authorizationRequest) {
                return authorizationRequest;
            }
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
