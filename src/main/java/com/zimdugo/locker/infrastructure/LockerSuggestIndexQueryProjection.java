package com.zimdugo.locker.infrastructure;

import java.time.LocalDateTime;

public interface LockerSuggestIndexQueryProjection {
    Long getLockerId();
    String getLockerName();
    String getRoadAddress();
    double getLockerLatitude();
    double getLockerLongitude();
    String getLockerType();
    String getIndoorOutdoorType();
    String getLockerSize();
    LocalDateTime getUpdatedAt();
    Long getPlaceId();
    String getPlaceName();
}
