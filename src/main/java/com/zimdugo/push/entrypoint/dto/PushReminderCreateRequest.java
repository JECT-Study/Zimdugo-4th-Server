package com.zimdugo.push.entrypoint.dto;

import com.zimdugo.push.application.PushReminderCreateCommand;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

public record PushReminderCreateRequest(
    @NotNull @Positive Long lockerId,
    @NotNull Instant startedAt,
    @NotNull @Future Instant endAt,
    Integer remindBeforeMinutes
) {
    public PushReminderCreateCommand toCommand(String deviceTokenHash) {
        return new PushReminderCreateCommand(deviceTokenHash, lockerId, startedAt, endAt, remindBeforeMinutes);
    }
}
