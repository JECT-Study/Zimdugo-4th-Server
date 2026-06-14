package com.zimdugo.common.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AcceptLanguageVaryFilterTest {

    private final AcceptLanguageVaryFilter filter = new AcceptLanguageVaryFilter();

    @Test
    @DisplayName("응답에 Accept-Language Vary 헤더를 추가한다")
    void addsAcceptLanguageVaryHeader() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
            new MockHttpServletRequest(),
            response,
            mock(FilterChain.class)
        );

        assertThat(response.getHeaders(HttpHeaders.VARY)).contains(HttpHeaders.ACCEPT_LANGUAGE);
    }
}
