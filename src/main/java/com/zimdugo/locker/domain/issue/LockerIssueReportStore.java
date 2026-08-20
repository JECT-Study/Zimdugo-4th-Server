package com.zimdugo.locker.domain.issue;

public interface LockerIssueReportStore {
    SavedLockerIssueReport create(LockerIssueReportCreateInfo createInfo);
}
