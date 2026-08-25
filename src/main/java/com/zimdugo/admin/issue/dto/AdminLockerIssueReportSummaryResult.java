package com.zimdugo.admin.issue.dto;

import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import java.time.LocalDateTime;

public record AdminLockerIssueReportSummaryResult(
    Long id,
    Long lockerId,
    String lockerName,
    String lockerRoadAddress,
    boolean lockerDeleted,
    LockerIssueReportType reportType,
    String detail,
    LockerIssueReportStatus status,
    LocalDateTime createdAt
) {
}
