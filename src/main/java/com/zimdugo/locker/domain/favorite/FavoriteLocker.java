package com.zimdugo.locker.domain.favorite;

import java.time.LocalDateTime;

public record FavoriteLocker(
    Long id,
    Long userId,
    Long lockerId,
    LocalDateTime createdAt
) {
}
