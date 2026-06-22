package com.zimdugo.user.infrastructure.visitor;

import com.zimdugo.user.domain.VisitorAccessEvent;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VisitorInterceptorTest {

    private static final String VISITOR_ID = "7d444840-9dc0-11d1-b245-5ffdce74fad2";
    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-06-22T03:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );

    private ApplicationEventPublisher eventPublisher;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private VisitorInterceptor interceptor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        interceptor = new VisitorInterceptor(eventPublisher, redisTemplate, CLOCK, true);
    }

    @Test
    void publishesOnlyWhenRedisClaimsFirstVisit() {
        request.setCookies(new Cookie("visitorId", VISITOR_ID));
        when(valueOperations.setIfAbsent(
            "visitor:20260622:" + VISITOR_ID,
            "visited",
            Duration.ofDays(1)
        )).thenReturn(true);

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();

        verify(eventPublisher).publishEvent(any(VisitorAccessEvent.class));
    }

    @Test
    void skipsEventWhenVisitWasAlreadyClaimed() {
        request.setCookies(new Cookie("visitorId", VISITOR_ID));
        when(valueOperations.setIfAbsent(anyString(), eq("visited"), eq(Duration.ofDays(1))))
            .thenReturn(false);

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void allowsRequestAndSkipsEventWhenRedisFails() {
        request.setCookies(new Cookie("visitorId", VISITOR_ID));
        when(valueOperations.setIfAbsent(anyString(), eq("visited"), eq(Duration.ofDays(1))))
            .thenThrow(new RedisConnectionFailureException("unavailable"));

        assertThat(interceptor.preHandle(request, response, new Object())).isTrue();

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void replacesInvalidCookieWithHardenedCookie() {
        request.setCookies(new Cookie("visitorId", "not-a-uuid"));
        when(valueOperations.setIfAbsent(anyString(), eq("visited"), eq(Duration.ofDays(1))))
            .thenReturn(false);

        interceptor.preHandle(request, response, new Object());

        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
            .contains("visitorId=")
            .contains("Path=/")
            .contains("Max-Age=31536000")
            .contains("Secure")
            .contains("HttpOnly")
            .contains("SameSite=Lax")
            .doesNotContain("not-a-uuid");
    }
}
