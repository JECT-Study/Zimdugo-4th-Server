package com.zimdugo.locker.application.issue;

import com.zimdugo.locker.domain.issue.LockerIssueReportCreateInfo;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;

public record LockerIssueReportCreateCommand(
    Long lockerId,
    String reportType,
    String detail
) {
    public LockerIssueReportCreateInfo toCreateInfo() {
        return new LockerIssueReportCreateInfo(
            lockerId,
            LockerIssueReportType.valueOf(reportType),
            detail,
            LockerIssueReportStatus.PENDING
        );
    }
}
