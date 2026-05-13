package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record FavoriteLocker(
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
