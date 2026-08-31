package com.zimdugo.push.application;

import com.zimdugo.push.domain.PushReminderSummary;
import java.time.Duration;
import java.time.Instant;

public record PushReminderResult(
    Long id,
    Long lockerId,
    Instant startedAt,
    Instant endAt,
    Integer totalUsageMinutes,
    Integer remainingMinutes,
    Integer remindBeforeMinutes
) {

    public static PushReminderResult from(PushReminderSummary summary, Instant now) {
        return new PushReminderResult(
            summary.id(),
            summary.lockerId(),
            summary.startedAt(),
            summary.endAt(),
            summary.totalUsageMinutes(),
            minutesCeiling(now, summary.endAt()),
            summary.remindBeforeMinutes()
        );
    }

    private static int minutesCeiling(Instant from, Instant to) {
        Duration duration = Duration.between(from, to);
        long fullMinutes = duration.toMinutes();
        return Math.toIntExact(duration.minusMinutes(fullMinutes).isZero() ? fullMinutes : fullMinutes + 1);
    }
}
