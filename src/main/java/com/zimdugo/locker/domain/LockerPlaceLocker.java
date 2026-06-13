package com.zimdugo.locker.domain;

import java.time.LocalDateTime;
import java.util.Set;

public record LockerPlaceLocker(
    Long placeId,
    Long lockerId,
    String lockerName,
    String roadAddress,
    LockerType lockerType,
    IndoorOutdoorType indoorOutdoorType,
    Set<LockerSizeType> lockerSizes,
    Integer minPrice,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt
) {
}
