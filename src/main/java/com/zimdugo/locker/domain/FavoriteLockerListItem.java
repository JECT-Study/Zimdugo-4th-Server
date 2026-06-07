package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record FavoriteLockerListItem(
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
