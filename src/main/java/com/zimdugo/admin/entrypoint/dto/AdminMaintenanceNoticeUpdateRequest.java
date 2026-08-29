package com.zimdugo.admin.entrypoint.dto;

import com.zimdugo.maintenance.application.dto.MaintenanceNoticeUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AdminMaintenanceNoticeUpdateRequest(
    @NotNull Boolean enabled,
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 2000) String message,
    @NotNull LocalDateTime startedAt,
    LocalDateTime endedAt
) {
    public MaintenanceNoticeUpdateCommand toCommand() {
        return new MaintenanceNoticeUpdateCommand(enabled.booleanValue(), title, message, startedAt, endedAt);
    }
}
