package com.zimdugo.auth.entrypoint.filter;

import com.zimdugo.auth.config.AuthProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class PushOriginValidationFilter extends OncePerRequestFilter {

    private static final String PUSH_API_PREFIX = "/api/v1/push/";
    private static final Set<String> SAFE_METHODS = Set.of(HttpMethod.GET.name(), HttpMethod.HEAD.name());

    private final AuthProperties authProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PUSH_API_PREFIX) || SAFE_METHODS.contains(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        Set<String> allowedOrigins = authProperties.getCallback().getAllowedOrigins().stream()
            .collect(Collectors.toUnmodifiableSet());
        // 로그인 없이 기기 식별자를 발급하므로, CSRF를 비활성화한 API 체인에서는 브라우저 Origin을 별도 검증
        if (origin == null || !allowedOrigins.contains(origin)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
