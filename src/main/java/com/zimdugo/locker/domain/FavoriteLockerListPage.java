package com.zimdugo.locker.domain;

import java.util.List;

public record FavoriteLockerListPage(
    List<FavoriteLockerListItem> items,
    long totalCount,
    boolean hasNext
) {
    public static FavoriteLockerListPage empty() {
        return new FavoriteLockerListPage(List.of(), 0, false);
    }
}
