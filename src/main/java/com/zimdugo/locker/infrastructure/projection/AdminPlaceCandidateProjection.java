package com.zimdugo.locker.infrastructure.projection;

public interface AdminPlaceCandidateProjection {
    Long getPlaceId();
    String getPlaceName();
    String getRoadAddress();
    Double getLatitude();
    Double getLongitude();
    Double getDistanceMeters();
    Boolean getExactAddress();
}
