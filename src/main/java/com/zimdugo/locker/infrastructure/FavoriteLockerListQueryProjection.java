package com.zimdugo.locker.infrastructure;

import java.time.LocalDateTime;

public interface FavoriteLockerListQueryProjection {
    Long getLockerId();

    String getLockerName();

    String getRoadAddress();

    String getLockerType();

    double getLatitude();

    double getLongitude();

    long getDistanceMeters();

    LocalDateTime getUpdatedAt();
}
