package com.zimdugo.locker.domain.locker;

public record NearbyLocker(
    Long id,
    double latitude,
    double longitude,
    Long placeId
) {
}
