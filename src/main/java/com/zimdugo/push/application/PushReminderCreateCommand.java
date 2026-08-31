package com.zimdugo.push.application;

import java.time.Instant;

public record PushReminderCreateCommand(
    String deviceTokenHash,
    Long lockerId,
    Instant startedAt,
    Instant endAt,
    Integer remindBeforeMinutes
) {
}
