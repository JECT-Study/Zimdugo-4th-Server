package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.NearbyLocker;

public record NearbyLockerResponse(
    Long id,
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    long distanceMeters
) {
    public static NearbyLockerResponse from(NearbyLocker nearbyLocker) {
        return new NearbyLockerResponse(
            nearbyLocker.id(),
            nearbyLocker.name(),
            nearbyLocker.roadAddress(),
            nearbyLocker.latitude(),
            nearbyLocker.longitude(),
            Math.round(nearbyLocker.distanceMeters())
        );
    }
}
