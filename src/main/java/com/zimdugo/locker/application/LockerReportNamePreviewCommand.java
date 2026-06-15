package com.zimdugo.locker.application;

public record LockerReportNamePreviewCommand(
    String roadAddress,
    double latitude,
    double longitude,
    String lockerType
) {
}
