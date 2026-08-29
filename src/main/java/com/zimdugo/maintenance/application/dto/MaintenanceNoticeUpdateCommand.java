package com.zimdugo.maintenance.application.dto;

import java.time.LocalDateTime;

public record MaintenanceNoticeUpdateCommand(
    boolean enabled,
    String title,
    String message,
    LocalDateTime startedAt,
    LocalDateTime endedAt
) {
}
