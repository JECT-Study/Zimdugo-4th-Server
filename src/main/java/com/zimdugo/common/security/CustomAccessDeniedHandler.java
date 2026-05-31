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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final String APPLICATION_JSON = "application/json";
    private static final String UTF_8 = "UTF-8";

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        RestResponse<Void> body = RestResponse.error(
            ErrorCode.FORBIDDEN,
            ErrorCode.FORBIDDEN.getMessage(),
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
