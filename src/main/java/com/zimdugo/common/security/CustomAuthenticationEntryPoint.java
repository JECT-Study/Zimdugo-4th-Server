package com.zimdugo.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.filter.RequestTraceFilter;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final String APPLICATION_JSON = "application/json";
    private static final String UTF_8 = "UTF-8";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        RestResponse<Void> body = RestResponse.error(
            ErrorCode.UNAUTHORIZED,
            ErrorCode.UNAUTHORIZED.getMessage(),
            request.getRequestURI(),
            resolveTraceId(request)
        );

        response.getWriter().write(objectMapper.writeValueAsString(body));
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
