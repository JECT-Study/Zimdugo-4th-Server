package com.zimdugo.locker.application.result.favorite;

import java.time.LocalDateTime;

public record FavoriteLockerListItemResult(
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt,
    boolean isFavorite
) {
}
