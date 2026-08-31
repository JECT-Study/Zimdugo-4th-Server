package com.zimdugo.push.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PushReminderRateLimiter {

    private final Map<String, Deque<Instant>> requests = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maximumRequests;
    private final Duration window;

    public PushReminderRateLimiter(PushReminderProperties properties, Clock clock) {
        this.clock = clock;
        this.maximumRequests = properties.maximumCreateRequests();
        this.window = Duration.ofSeconds(properties.createRateLimitWindowSeconds());
    }

    public void check(String deviceTokenHash) {
        Deque<Instant> timestamps = requests.computeIfAbsent(deviceTokenHash, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            // 동일 기기의 확인과 기록을 원자적으로 처리해 동시 요청이 한도를 우회하지 못하게 한다.
            Instant threshold = clock.instant().minus(window);
            while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(threshold)) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maximumRequests) {
                throw new BusinessException(ErrorCode.PUSH_RATE_LIMIT_EXCEEDED);
            }
            timestamps.addLast(clock.instant());
        }
    }

    @Scheduled(fixedDelayString = "${push.reminder.rate-limit-cleanup-fixed-delay-millis:60000}")
    void cleanupExpiredRequests() {
        Instant threshold = clock.instant().minus(window);
        requests.entrySet().removeIf(entry -> removeWhenInactive(entry.getValue(), threshold));
    }

    private boolean removeWhenInactive(Deque<Instant> timestamps, Instant threshold) {
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(threshold)) {
                timestamps.removeFirst();
            }
            return timestamps.isEmpty();
        }
    }
}
