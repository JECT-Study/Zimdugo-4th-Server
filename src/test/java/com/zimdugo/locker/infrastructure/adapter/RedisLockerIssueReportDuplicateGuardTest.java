package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.core.exception.BusinessException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
            eq("locker-issue-report:1:visitor-1"),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(true);

        assertThatCode(() -> guard.checkAndReserve(1L, "visitor-1"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("짧은 시간 내 동일 보관함 반복 신고는 막는다")
    void blocksDuplicateReport() {
        when(valueOperations.setIfAbsent(
            eq("locker-issue-report:1:visitor-1"),
            eq("reported"),
            eq(Duration.ofMinutes(10))
        )).thenReturn(false);

        assertThatThrownBy(() -> guard.checkAndReserve(1L, "visitor-1"))
            .isInstanceOf(BusinessException.class);
    }
}
