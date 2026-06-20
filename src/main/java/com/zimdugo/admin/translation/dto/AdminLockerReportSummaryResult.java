package com.zimdugo.admin.translation.dto;

import com.zimdugo.locker.domain.report.LockerReportStatus;
import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import java.time.LocalDateTime;

public record AdminLockerReportSummaryResult(
    Long id,
    String name,
    String roadAddress,
    LockerReportStatus status,
    Long appliedPlaceId,
    Long appliedLockerId,
    LocalDateTime createdAt
) {
    public static AdminLockerReportSummaryResult from(LockerReportEntity report) {
        return new AdminLockerReportSummaryResult(
            report.getId(),
            report.getName(),
            report.getRoadAddress(),
            report.getStatus(),
            report.getAppliedPlaceId(),
            report.getAppliedLockerId(),
            report.getCreatedAt()
        );
    }
}
