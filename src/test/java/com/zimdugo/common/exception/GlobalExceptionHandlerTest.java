package com.zimdugo.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.common.filter.RequestTraceFilter;
import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.core.response.RestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void hidesInternalBusinessExceptionMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        BusinessException exception = new BusinessException(
            ErrorCode.INTERNAL_SERVER_ERROR,
            "internal detail"
        );

        ResponseEntity<RestResponse<Void>> response = handler.handleBusinessException(exception, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    @Test
    void preservesClientBusinessExceptionMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        BusinessException exception = new BusinessException(ErrorCode.BAD_REQUEST, "잘못된 상세 요청입니다.");

        ResponseEntity<RestResponse<Void>> response = handler.handleBusinessException(exception, request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("잘못된 상세 요청입니다.");
    }

    @Test
    void readsGeneratedTraceIdFromRequestAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
        request.setAttribute(RequestTraceFilter.TRACE_ID_ATTRIBUTE, "generated-trace-id");

        ResponseEntity<RestResponse<Void>> response = handler.handleBusinessException(
            new BusinessException(ErrorCode.BAD_REQUEST),
            request
        );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTraceId()).isEqualTo("generated-trace-id");
    }
}
