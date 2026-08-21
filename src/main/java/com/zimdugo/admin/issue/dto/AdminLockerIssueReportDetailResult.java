package com.zimdugo.admin.issue.dto;

import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import java.time.LocalDateTime;

public record AdminLockerIssueReportDetailResult(
    Long id,
    Long lockerId,
    String lockerName,
    String lockerRoadAddress,
    LockerIssueReportType reportType,
    String detail,
    LockerIssueReportStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String reviewedBy,
    String reviewNote,
    LocalDateTime reviewedAt,
    boolean lockerDeleted
) {
}
