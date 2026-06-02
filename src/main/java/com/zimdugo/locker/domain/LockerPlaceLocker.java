package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record LockerPlaceLocker(
    Long placeId,
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt
) {
}
