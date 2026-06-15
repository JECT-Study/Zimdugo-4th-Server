package com.zimdugo.locker.application.result.report;

public record LockerReportNamePreviewResult(
    String name,
    String roadAddress,
    double latitude,
    double longitude
) {
}
