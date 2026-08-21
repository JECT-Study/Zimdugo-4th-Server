package com.zimdugo.locker.domain.issue;

public interface LockerIssueReportDuplicateGuard {
    void checkAndReserve(Long lockerId, String reporterIdentifier, String clientIpAddress);
}
