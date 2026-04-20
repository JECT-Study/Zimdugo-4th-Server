package com.zimdugo.auth.entrypoint;

import com.zimdugo.auth.application.OAuth2CallbackUrlCookieManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class OAuth2CallbackUrlCaptureFilter extends OncePerRequestFilter {

    private static final String OAUTH2_AUTHORIZATION_REQUEST_PREFIX = "/oauth2/authorization/";

    private final OAuth2CallbackUrlCookieManager callbackUrlCookieManager;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        callbackUrlCookieManager.saveCallbackUrl(request, response);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(OAUTH2_AUTHORIZATION_REQUEST_PREFIX);
    }
}
