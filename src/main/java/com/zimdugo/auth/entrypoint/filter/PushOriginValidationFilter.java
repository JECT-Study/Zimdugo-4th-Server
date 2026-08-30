package com.zimdugo.auth.entrypoint.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.auth.config.AuthProperties;
import com.zimdugo.common.filter.RequestTraceFilter;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class PushOriginValidationFilter extends OncePerRequestFilter {

    private static final String PUSH_API_PREFIX = "/api/v1/push/";
    private static final Set<String> SAFE_METHODS = Set.of(HttpMethod.GET.name(), HttpMethod.HEAD.name());

    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

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
            writeForbiddenResponse(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeForbiddenResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(RestResponse.error(
            ErrorCode.FORBIDDEN,
            ErrorCode.FORBIDDEN.getMessage(),
            request.getRequestURI(),
            resolveTraceId(request)
        )));
    }

    private String resolveTraceId(HttpServletRequest request) {
        Object traceIdAttribute = request.getAttribute(RequestTraceFilter.TRACE_ID_ATTRIBUTE);
        if (traceIdAttribute instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }
        String traceId = request.getHeader(RequestTraceFilter.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            return null;
        }
        return traceId;
    }
}
