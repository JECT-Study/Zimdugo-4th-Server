package com.zimdugo.push.domain;

import java.time.Instant;

public record PushReminderSummary(
    Long id,
    Long lockerId,
    Instant startedAt,
    Instant endAt,
    Integer totalUsageMinutes,
    Integer remindBeforeMinutes
) {

    public PushReminderSummary {
        if (startedAt == null || endAt == null || !startedAt.isBefore(endAt)) {
            throw new IllegalArgumentException("리마인더 시작 시각은 종료 시각보다 이전이어야 합니다.");
        }
    }
}
