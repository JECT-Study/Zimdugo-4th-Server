package com.zimdugo.locker.infrastructure;

import java.time.LocalDateTime;
import java.time.LocalTime;

public interface LockerDetailQueryProjection {
    Long getLockerId();
    String getLockerName();
    String getRoadAddress();
    double getLatitude();
    double getLongitude();
    Long getPlaceId();
    String getPlaceName();
    String getLockerType();
    String getIndoorOutdoorType();
    String getGroundLevelType();
    Integer getFloor();
    Integer getMinPrice();
    Integer getMaxPrice();
    String getLockerSizes();
    String getDetailInfo();
    LocalTime getStartTime();
    LocalTime getEndTime();
    String getImageUrl();
    int getAccurateVoteCount();
    int getInaccurateVoteCount();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
