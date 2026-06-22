package com.zimdugo.user.infrastructure.visitor;

import com.zimdugo.user.domain.VisitorAccessEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class VisitorInterceptor implements HandlerInterceptor {

    private static final String VISITOR_COOKIE_NAME = "visitorId";
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;

    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) {
        String visitorId = getOrCreateVisitorId(request, response);
        Long userId = extractUserId(request);

        LocalDateTime now = LocalDateTime.now();
        LocalDate date = now.toLocalDate();

        String redisKey = "visitor:" + date.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ":" + visitorId;
        Boolean hasVisited = stringRedisTemplate.hasKey(redisKey);

        if (Boolean.TRUE.equals(hasVisited)) {
            return true;
        }

        stringRedisTemplate.opsForValue().set(redisKey, "visited", Duration.ofDays(1));
        eventPublisher.publishEvent(new VisitorAccessEvent(visitorId, userId, date, now));
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

        if (visitorId == null || visitorId.isBlank()) {
            visitorId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie(VISITOR_COOKIE_NAME, visitorId);
            cookie.setPath("/");
            cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS); // 1 year
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        return visitorId;
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
