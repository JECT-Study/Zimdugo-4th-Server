package com.zimdugo.locker.application.result.issue;

import com.zimdugo.locker.domain.issue.SavedLockerIssueReport;
import java.time.LocalDateTime;

public record LockerIssueReportCreateResult(
    Long reportId,
    LocalDateTime createdAt
) {
    public static LockerIssueReportCreateResult from(SavedLockerIssueReport report) {
        return new LockerIssueReportCreateResult(
            report.id(),
            report.createdAt()
        );
    }
}
