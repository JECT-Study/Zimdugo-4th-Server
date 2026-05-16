package com.zimdugo.locker.application;

import java.util.List;

public record FavoriteLockerResponse(
    long totalCount,
    int page,
    int size,
    boolean hasNext,
    List<FavoriteLockerItemResponse> items
) {
}
