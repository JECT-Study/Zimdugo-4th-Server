package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record LockerReportImageMetadata(
    String metadataJson,
    LocalDateTime extractedAt,
    Double gpsLatitude,
    Double gpsLongitude,
    Double gpsAltitude,
    LocalDateTime capturedAt
) {
    public static LockerReportImageMetadata empty() {
        return new LockerReportImageMetadata(null, null, null, null, null, null);
    }
}
