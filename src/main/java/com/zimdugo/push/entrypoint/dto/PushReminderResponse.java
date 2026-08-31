package com.zimdugo.push.entrypoint.dto;

import com.zimdugo.push.application.PushReminderResult;
import java.time.Instant;

public record PushReminderResponse(
    Long id,
    Long lockerId,
    Instant startedAt,
    Instant endAt,
    Integer totalUsageMinutes,
    Integer remainingMinutes,
    Integer remindBeforeMinutes
) {

    public static PushReminderResponse from(PushReminderResult result) {
        return new PushReminderResponse(
            result.id(),
            result.lockerId(),
            result.startedAt(),
            result.endAt(),
            result.totalUsageMinutes(),
            result.remainingMinutes(),
            result.remindBeforeMinutes()
        );
    }
}
