package com.zimdugo.common.filter;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestTraceFilterTest {

    private final RequestTraceFilter filter = new RequestTraceFilter();

    @Test
    void setsTraceIdHeaderAndClearsMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/lockers");
        request.addHeader(RequestTraceFilter.TRACE_ID_HEADER, "request-trace-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
            assertThat(MDC.get("traceId")).isEqualTo("request-trace-id")
        );

        assertThat(response.getHeader(RequestTraceFilter.TRACE_ID_HEADER)).isEqualTo("request-trace-id");
        assertThat(request.getAttribute(RequestTraceFilter.TRACE_ID_ATTRIBUTE)).isEqualTo("request-trace-id");
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    void doesNotLogSuccessfulManagementPollingRequests() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestTraceFilter.class);
        Level originalLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);

        try {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
            MockHttpServletResponse response = new MockHttpServletResponse();

            filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            });

            assertThat(appender.list).isEmpty();
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(originalLevel);
        }
    }
}
