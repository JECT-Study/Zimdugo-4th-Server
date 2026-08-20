package com.zimdugo.locker.domain.issue;

import java.time.LocalDateTime;

public record SavedLockerIssueReport(
    Long id,
    Long lockerId,
    LockerIssueReportType reportType,
    String detail,
    LockerIssueReportStatus status,
    LocalDateTime createdAt
) {
}
