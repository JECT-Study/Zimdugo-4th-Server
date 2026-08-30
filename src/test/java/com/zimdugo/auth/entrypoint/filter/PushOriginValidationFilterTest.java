package com.zimdugo.auth.entrypoint.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.zimdugo.auth.config.AuthProperties;
import com.zimdugo.common.config.JacksonConfig;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class PushOriginValidationFilterTest {

    @Test
    void returnsCommonErrorResponseWhenOriginIsNotAllowed() throws Exception {
        AuthProperties authProperties = new AuthProperties();
        authProperties.getCallback().setAllowedOrigins(java.util.List.of("https://zimdugo.com"));
        PushOriginValidationFilter filter = new PushOriginValidationFilter(
            authProperties,
            new JacksonConfig().objectMapper()
        );
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/push/subscriptions");
        request.addHeader("Origin", "https://attacker.example");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("\"code\":\"COMMON-403\"");
        assertThat(response.getContentAsString()).contains("\"path\":\"/api/v1/push/subscriptions\"");
        verifyNoInteractions(filterChain);
    }
}
