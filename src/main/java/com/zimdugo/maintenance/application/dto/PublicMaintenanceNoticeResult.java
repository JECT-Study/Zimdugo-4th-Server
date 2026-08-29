package com.zimdugo.maintenance.application.dto;

import com.zimdugo.maintenance.domain.MaintenanceNotice;
import java.time.LocalDateTime;

public record PublicMaintenanceNoticeResult(
    boolean isUnderMaintenance,
    String title,
    String message,
    LocalDateTime startedAt,
    LocalDateTime endedAt
) {
    public static PublicMaintenanceNoticeResult from(MaintenanceNotice notice) {
        return new PublicMaintenanceNoticeResult(
            notice.enabled(), notice.title(), notice.message(), notice.startedAt(), notice.endedAt()
        );
    }

    public static PublicMaintenanceNoticeResult inactive() {
        return new PublicMaintenanceNoticeResult(false, null, null, null, null);
    }
}
