package com.zimdugo.locker.infrastructure;

public interface NearbyLockerQueryProjection {
    Long getId();
    String getName();
    String getRoadAddress();
    double getLatitude();
    double getLongitude();
    double getDistanceMeters();
}
