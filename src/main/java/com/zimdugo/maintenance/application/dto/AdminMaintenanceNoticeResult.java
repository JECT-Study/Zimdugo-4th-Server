package com.zimdugo.maintenance.application.dto;

import com.zimdugo.maintenance.domain.MaintenanceNotice;
import java.time.LocalDateTime;

public record AdminMaintenanceNoticeResult(
    boolean enabled,
    String title,
    String message,
    LocalDateTime startedAt,
    LocalDateTime endedAt
) {
    public static AdminMaintenanceNoticeResult from(MaintenanceNotice notice) {
        return new AdminMaintenanceNoticeResult(
            notice.enabled(), notice.title(), notice.message(), notice.startedAt(), notice.endedAt()
        );
    }

    public static AdminMaintenanceNoticeResult empty() {
        return new AdminMaintenanceNoticeResult(false, null, null, null, null);
    }
}
