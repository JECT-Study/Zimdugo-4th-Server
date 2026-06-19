package com.zimdugo.common.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
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
}
