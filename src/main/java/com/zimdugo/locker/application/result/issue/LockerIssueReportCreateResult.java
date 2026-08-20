package com.zimdugo.locker.application.result.issue;

import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;
import com.zimdugo.locker.domain.issue.SavedLockerIssueReport;
import java.time.LocalDateTime;

public record LockerIssueReportCreateResult(
    Long reportId,
    Long lockerId,
    LockerIssueReportType reportType,
    String detail,
    LockerIssueReportStatus status,
    LocalDateTime createdAt
) {
    public static LockerIssueReportCreateResult from(SavedLockerIssueReport report) {
        return new LockerIssueReportCreateResult(
            report.id(),
            report.lockerId(),
            report.reportType(),
            report.detail(),
            report.status(),
            report.createdAt()
        );
    }
}
