package com.zimdugo.push.application;

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
}
