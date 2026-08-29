package com.zimdugo.maintenance.domain;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.time.LocalDateTime;

public record MaintenanceNotice(
    boolean enabled,
    String title,
    String message,
    LocalDateTime startedAt,
    LocalDateTime endedAt
) {
    public static MaintenanceNotice of(
        boolean enabled,
        String title,
        String message,
        LocalDateTime startedAt,
        LocalDateTime endedAt
    ) {
        if (endedAt != null && endedAt.isBefore(startedAt)) {
            throw new BusinessException(ErrorCode.INVALID_MAINTENANCE_NOTICE_PERIOD);
        }
        return new MaintenanceNotice(enabled, title, message, startedAt, endedAt);
    }
}
