package com.zimdugo.auth.application;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final OAuth2CallbackUrlCookieManager callbackUrlCookieManager;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        String callbackUrl = callbackUrlCookieManager.resolveCallbackUrl(request);
        callbackUrlCookieManager.clearCallbackUrl(response);

        log.warn("oauth login failure. callbackUrl={}, reason={}", callbackUrl, exception.getMessage());
        response.sendRedirect(appendCode(callbackUrl, "LOGIN_FAILED"));
    }

    private String appendCode(String callbackUrl, String code) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
            .replaceQueryParam("code", code)
            .build(true)
            .toUriString();
    }
}

