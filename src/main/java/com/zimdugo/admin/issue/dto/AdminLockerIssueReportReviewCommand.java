package com.zimdugo.admin.issue.dto;

public record AdminLockerIssueReportReviewCommand(
    Long reportId,
    String reviewMemo,
    String reviewer
) {
}
