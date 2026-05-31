package com.zimdugo.locker.domain;

public record NearbyLocker(
    Long id,
    double latitude,
    double longitude,
    Long placeId
) {
}
