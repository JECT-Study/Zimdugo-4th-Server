package com.zimdugo.locker.infrastructure;

import java.time.LocalDateTime;

public interface LockerPlaceLockerQueryProjection {
    Long getPlaceId();
    Long getLockerId();
    String getLockerName();
    String getRoadAddress();
    String getLockerType();
    String getIndoorOutdoorType();
    String getLockerSize();
    Integer getMinPrice();
    double getLockerLatitude();
    double getLockerLongitude();
    double getDistanceMeters();
    LocalDateTime getUpdatedAt();
}
