package com.zimdugo.locker.domain;

import java.util.List;

public record FavoriteLockerPage(
    long totalCount,
    int page,
    int size,
    boolean hasNext,
    List<FavoriteLocker> favorites
) {
}
