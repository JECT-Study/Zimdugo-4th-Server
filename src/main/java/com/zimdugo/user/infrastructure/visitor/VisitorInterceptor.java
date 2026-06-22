package com.zimdugo.user.infrastructure.visitor;

import com.zimdugo.user.domain.VisitorAccessEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class VisitorInterceptor implements HandlerInterceptor {

    private static final String VISITOR_COOKIE_NAME = "visitorId";
    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(365);
    private static final Duration VISIT_CACHE_TTL = Duration.ofDays(1);

    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate stringRedisTemplate;
    private final Clock clock;
    private final boolean secureCookie;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        String visitorId = getOrCreateVisitorId(request, response);
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate date = now.toLocalDate();

        String redisKey = "visitor:" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + ":" + visitorId;
        try {
            Boolean firstVisit = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "visited", VISIT_CACHE_TTL);
            if (Boolean.TRUE.equals(firstVisit)) {
                eventPublisher.publishEvent(new VisitorAccessEvent(
                    visitorId,
                    extractUserId(request),
                    date,
                    now
                ));
            }
        } catch (RuntimeException exception) {
            log.warn("방문자 통계 수집 실패로 요청 기록 생략", exception);
        }
        return true;
    }

    private String getOrCreateVisitorId(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String visitorId = null;

        if (cookies != null) {
            visitorId = Arrays.stream(cookies)
                .filter(cookie -> VISITOR_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
        }

        if (!isValidVisitorId(visitorId)) {
            visitorId = UUID.randomUUID().toString();
            addVisitorCookie(response, visitorId);
        }

        return visitorId;
    }

    private boolean isValidVisitorId(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return false;
        }

        try {
            return UUID.fromString(visitorId).toString().equalsIgnoreCase(visitorId);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void addVisitorCookie(HttpServletResponse response, String visitorId) {
        ResponseCookie cookie = ResponseCookie.from(VISITOR_COOKIE_NAME, visitorId)
            .httpOnly(true)
            .secure(secureCookie)
            .sameSite("Lax")
            .path("/")
            .maxAge(COOKIE_MAX_AGE)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private Long extractUserId(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
