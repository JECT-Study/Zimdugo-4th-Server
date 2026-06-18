package com.zimdugo.locker.infrastructure.projection;

public interface PlaceDetailQueryProjection {
    Long getPlaceId();
    String getPlaceName();
    String getRoadAddress();
    double getLatitude();
    double getLongitude();
}
