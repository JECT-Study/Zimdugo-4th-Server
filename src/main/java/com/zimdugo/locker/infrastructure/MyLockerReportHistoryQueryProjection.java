package com.zimdugo.locker.infrastructure;

import java.time.LocalDateTime;

public interface MyLockerReportHistoryQueryProjection {
    Long getReportId();

    String getLockerName();

    String getRoadAddress();

    String getLockerType();

    double getLatitude();

    double getLongitude();

    long getDistanceMeters();

    LocalDateTime getUpdatedAt();
}
