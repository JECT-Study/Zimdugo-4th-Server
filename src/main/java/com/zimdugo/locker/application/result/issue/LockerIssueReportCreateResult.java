package com.zimdugo.locker.application.result.issue;

import com.zimdugo.locker.domain.issue.SavedLockerIssueReport;
import java.time.LocalDateTime;

public record LockerIssueReportCreateResult(
    Long reportId,
    Long lockerId,
    String reportType,
    String detail,
    String status,
    LocalDateTime createdAt
) {
    public static LockerIssueReportCreateResult from(SavedLockerIssueReport report) {
        return new LockerIssueReportCreateResult(
            report.id(),
            report.lockerId(),
            report.reportType().name(),
            report.detail(),
            report.status().name(),
            report.createdAt()
        );
    }
}
