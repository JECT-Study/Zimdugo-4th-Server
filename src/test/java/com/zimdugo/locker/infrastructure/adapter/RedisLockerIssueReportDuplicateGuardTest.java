package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.core.exception.BusinessException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisLockerIssueReportDuplicateGuardTest {

    private StringRedisTemplate stringRedisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RedisLockerIssueReportDuplicateGuard guard;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        guard = new RedisLockerIssueReportDuplicateGuard(stringRedisTemplate);
    }

    @Test
    @DisplayName("첫 신고는 허용한다")
    void allowsFirstReport() {
        when(valueOperations.setIfAbsent(
            eq("locker-issue-report:duplicate:visitor:1:visitor-1"),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(true);
        when(valueOperations.setIfAbsent(
            anyString(),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(true);
        when(valueOperations.increment(eq(hourlyKey("visitor", "visitor-1")))).thenReturn(1L);
        when(valueOperations.increment(eq(hourlyKey("ip", hashedIp("127.0.0.1"))))).thenReturn(1L);

        assertThatCode(() -> guard.checkAndReserve(1L, "visitor-1", "127.0.0.1"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("다른 방문자는 같은 IP여도 동일 보관함 신고를 할 수 있다")
    void allowsDuplicateReportForDifferentVisitorOnSameIp() {
        when(valueOperations.setIfAbsent(
            eq("locker-issue-report:duplicate:visitor:1:visitor-1"),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(true);
        when(valueOperations.setIfAbsent(
            eq("locker-issue-report:duplicate:visitor:1:visitor-2"),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(true);
        when(valueOperations.increment(eq(hourlyKey("visitor", "visitor-1")))).thenReturn(1L);
        when(valueOperations.increment(eq(hourlyKey("visitor", "visitor-2")))).thenReturn(1L);
        when(valueOperations.increment(eq(hourlyKey("ip", hashedIp("127.0.0.1")))))
            .thenReturn(1L, 2L);

        assertThatCode(() -> guard.checkAndReserve(1L, "visitor-1", "127.0.0.1"))
            .doesNotThrowAnyException();
        assertThatCode(() -> guard.checkAndReserve(1L, "visitor-2", "127.0.0.1"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("짧은 시간 내 동일 보관함 반복 신고는 막는다")
    void blocksDuplicateReport() {
        when(valueOperations.setIfAbsent(
            eq("locker-issue-report:duplicate:visitor:1:visitor-1"),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(false);

        assertThatThrownBy(() -> guard.checkAndReserve(1L, "visitor-1", "127.0.0.1"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("방문자 기준 시간당 신고 횟수를 초과하면 막는다")
    void blocksVisitorRateLimit() {
        when(valueOperations.setIfAbsent(anyString(), eq("reported"), eq(Duration.ofMinutes(10))))
            .thenReturn(true);
        when(valueOperations.increment(eq(hourlyKey("visitor", "visitor-1")))).thenReturn(6L);

        assertThatThrownBy(() -> guard.checkAndReserve(1L, "visitor-1", null))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("IP 기준 시간당 신고 횟수를 초과하면 막는다")
    void blocksIpRateLimit() {
        when(valueOperations.setIfAbsent(
            eq("locker-issue-report:duplicate:visitor:1:visitor-1"),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        ))
            .thenReturn(true);
        when(valueOperations.increment(eq(hourlyKey("visitor", "visitor-1")))).thenReturn(1L);
        when(valueOperations.increment(eq(hourlyKey("ip", hashedIp("127.0.0.1"))))).thenReturn(21L);

        assertThatThrownBy(() -> guard.checkAndReserve(1L, "visitor-1", "127.0.0.1"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("방문자 식별자가 없으면 IP 기준으로 동일 보관함 중복 신고를 막는다")
    void blocksDuplicateReportByIpWhenVisitorIdentifierMissing() {
        when(valueOperations.setIfAbsent(
            eq("locker-issue-report:duplicate:ip:1:" + hashedIp("127.0.0.1")),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(false);

        assertThatThrownBy(() -> guard.checkAndReserve(1L, null, "127.0.0.1"))
            .isInstanceOf(BusinessException.class);
    }

    private String hourlyKey(String scope, String identifier) {
        String hourSlot = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        return "locker-issue-report:rate:" + scope + ":" + hourSlot + ":" + identifier;
    }

    private String hashedIp(String ipAddress) {
        try {
            return java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256").digest(
                    ipAddress.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                )
            );
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
