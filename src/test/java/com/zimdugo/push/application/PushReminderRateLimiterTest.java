package com.zimdugo.push.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PushReminderRateLimiterTest {

    @Test
    void rejectsRequestOverConfiguredDeviceLimit() {
        PushReminderRateLimiter limiter = new PushReminderRateLimiter(
            new PushReminderProperties(60, 86_400, 1, Set.of(5), 2, 60, 60_000),
            Clock.fixed(Instant.parse("2026-08-28T12:00:00Z"), ZoneOffset.UTC)
        );

        limiter.check("device-token-hash");
        limiter.check("device-token-hash");

        assertThatThrownBy(() -> limiter.check("device-token-hash"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.PUSH_RATE_LIMIT_EXCEEDED);
    }

    @Test
    void removesInactiveDeviceRateLimitHistory() throws ReflectiveOperationException {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-28T12:00:00Z"));
        PushReminderRateLimiter limiter = new PushReminderRateLimiter(
            new PushReminderProperties(60, 86_400, 1, Set.of(5), 2, 60, 60_000),
            clock
        );
        limiter.check("inactive-device-token-hash");
        clock.advanceSeconds(61);

        limiter.cleanupExpiredRequests();

        assertThat(requestHistory(limiter)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> requestHistory(PushReminderRateLimiter limiter) throws ReflectiveOperationException {
        Field requests = PushReminderRateLimiter.class.getDeclaredField("requests");
        requests.setAccessible(true);
        return (Map<String, ?>) requests.get(limiter);
    }

    private static class MutableClock extends Clock {

        private Instant current;

        private MutableClock(Instant current) {
            this.current = current;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current;
        }

        private void advanceSeconds(long seconds) {
            current = current.plusSeconds(seconds);
        }
    }
}
