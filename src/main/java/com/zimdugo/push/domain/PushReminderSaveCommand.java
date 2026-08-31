package com.zimdugo.push.domain;

import java.time.Instant;

public record PushReminderSaveCommand(
    Long deviceId,
    Long lockerId,
    Instant startedAt,
    Instant endAt,
    Integer totalUsageMinutes,
    Integer remindBeforeMinutes
) {
}
