package com.zimdugo.locker.entrypoint.dto.response.favorite;

import com.zimdugo.locker.application.result.favorite.FavoriteLockerListResult;
import java.util.List;

public record FavoriteLockerListResponse(
    int count,
    long totalCount,
    boolean hasNext,
    List<FavoriteLockerListItemResponse> items
) {
    public static FavoriteLockerListResponse from(FavoriteLockerListResult result) {
        List<FavoriteLockerListItemResponse> items = result.items().stream()
            .map(FavoriteLockerListItemResponse::from)
            .toList();

        return new FavoriteLockerListResponse(
            result.count(),
            result.totalCount(),
            result.hasNext(),
            items
        );
    }
}
