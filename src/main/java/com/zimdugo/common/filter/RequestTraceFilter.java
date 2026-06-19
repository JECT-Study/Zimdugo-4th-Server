package com.zimdugo.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RequestTraceFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Request-Id";
    public static final String TRACE_ID_ATTRIBUTE = "requestTraceId";
    private static final String MDC_TRACE_ID_KEY = "traceId";
    private static final int SERVER_ERROR_STATUS_THRESHOLD = 500;
    private static final long NANOS_PER_MILLISECOND = 1_000_000L;
    private static final String ACTUATOR_PATH_PREFIX = "/actuator";
    private static final String SPRING_BOOT_ADMIN_INSTANCE_PATH = "/instances";

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String traceId = resolveOrCreateTraceId(request);
        request.setAttribute(TRACE_ID_ATTRIBUTE, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        MDC.put(MDC_TRACE_ID_KEY, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / NANOS_PER_MILLISECOND;
            logRequestCompleted(request, response, durationMs);
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }

    private void logRequestCompleted(
        HttpServletRequest request,
        HttpServletResponse response,
        long durationMs
    ) {
        int status = response.getStatus();
        if (status >= SERVER_ERROR_STATUS_THRESHOLD) {
            log.warn(
                "요청 처리 완료. method={}, path={}, status={}, durationMs={}",
                request.getMethod(),
                request.getRequestURI(),
                status,
                durationMs
            );
            return;
        }
        if (isManagementPollingPath(request.getRequestURI())) {
            log.debug(
                "관리용 요청 처리 완료. method={}, path={}, status={}, durationMs={}",
                request.getMethod(),
                request.getRequestURI(),
                status,
                durationMs
            );
            return;
        }
        log.info(
            "요청 처리 완료. method={}, path={}, status={}, durationMs={}",
            request.getMethod(),
            request.getRequestURI(),
            status,
            durationMs
        );
    }

    private boolean isManagementPollingPath(String path) {
        return path != null
            && (path.startsWith(ACTUATOR_PATH_PREFIX) || path.equals(SPRING_BOOT_ADMIN_INSTANCE_PATH));
    }

    private String resolveOrCreateTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return traceId;
    }
}
