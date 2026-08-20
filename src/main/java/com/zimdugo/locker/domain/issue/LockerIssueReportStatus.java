package com.zimdugo.locker.domain.issue;

public enum LockerIssueReportStatus {
    PENDING("접수"),
    RESOLVED("처리 완료"),
    REJECTED("반려");

    private final String displayName;

    LockerIssueReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
