package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record NearbyLocker(
    Long id,
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    double distanceMeters,
    String lockerType,
    LocalDateTime updatedAt,
    Boolean isFavorite,
    Long placeId,
    String placeName,
    int matchRank
) {
}
