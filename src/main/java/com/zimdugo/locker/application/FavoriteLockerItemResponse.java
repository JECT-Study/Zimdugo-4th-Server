package com.zimdugo.locker.application;

import java.time.LocalDateTime;

public record FavoriteLockerItemResponse(
    Long lockerId,
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    LocalDateTime favoritedAt,
    LocalDateTime lastCompletedVoteAt,
    Long distanceMeters
) {
}
