package com.zimdugo.locker.application.result.report;

public record LockerReportCreateResult(
    Long reportId,
    Long lockerId,
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    String reportStatus
) {
}
