package com.zimdugo.locker.application.result.favorite;

import java.util.List;

public record FavoriteLockerListResult(
    int count,
    long totalCount,
    boolean hasNext,
    List<FavoriteLockerListItemResult> items
) {
    public static FavoriteLockerListResult empty() {
        return new FavoriteLockerListResult(0, 0, false, List.of());
    }

    public static FavoriteLockerListResult of(
        List<FavoriteLockerListItemResult> items,
        long totalCount,
        boolean hasNext
    ) {
        return new FavoriteLockerListResult(items.size(), totalCount, hasNext, items);
    }
}
