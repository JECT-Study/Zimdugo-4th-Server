package com.zimdugo.locker.application.issue;

import com.zimdugo.locker.domain.issue.LockerIssueReportCreateInfo;
import com.zimdugo.locker.domain.issue.LockerIssueReportStatus;
import com.zimdugo.locker.domain.issue.LockerIssueReportType;

public record LockerIssueReportCreateCommand(
    Long lockerId,
    LockerIssueReportType reportType,
    String detail
) {
    public LockerIssueReportCreateInfo toCreateInfo() {
        return new LockerIssueReportCreateInfo(
            lockerId,
            reportType,
            detail,
            LockerIssueReportStatus.PENDING
        );
    }
}
