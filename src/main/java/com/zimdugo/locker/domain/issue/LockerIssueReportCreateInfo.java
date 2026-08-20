package com.zimdugo.locker.domain.issue;

public record LockerIssueReportCreateInfo(
    Long lockerId,
    LockerIssueReportType reportType,
    String detail,
    LockerIssueReportStatus status
) {
}
