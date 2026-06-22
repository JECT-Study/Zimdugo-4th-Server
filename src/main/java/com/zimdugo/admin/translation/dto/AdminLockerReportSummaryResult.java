package com.zimdugo.admin.translation.dto;

import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.projection.AdminLockerReportListProjection;
import java.time.LocalDateTime;

public record AdminLockerReportSummaryResult(
    Long id,
    String name,
    String roadAddress,
    LockerReportStatus status,
    LocalDateTime appliedAt,
    LocalDateTime createdAt
) {
    public static AdminLockerReportSummaryResult from(AdminLockerReportListProjection report) {
        return new AdminLockerReportSummaryResult(
            report.getId(),
            report.getName(),
            report.getRoadAddress(),
            report.getStatus(),
            report.getAppliedAt(),
            report.getCreatedAt()
        );
    }
}
