package com.zimdugo.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final String APPLICATION_JSON = "application/json";
    private static final String UTF_8 = "UTF-8";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException {
        response.setStatus(ErrorCode.FORBIDDEN.httpStatus().value());
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF_8);

        Map<String, Object> body = ErrorResponse.of(ErrorCode.FORBIDDEN, request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
