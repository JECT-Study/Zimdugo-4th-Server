package com.zimdugo.locker.application;

public record LockerReportDuplicateCandidateResponse(
    Long lockerId,
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    double distanceMeters
) {
}
