package com.zimdugo.locker.domain;

public record NearbyLocker(
    Long id,
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    double distanceMeters
) {
}
