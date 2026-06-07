package com.zimdugo.locker.entrypoint.dto.response.favorite;

import com.zimdugo.locker.application.result.favorite.FavoriteLockerListItemResult;
import java.time.LocalDateTime;

public record FavoriteLockerListItemResponse(
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
    public static FavoriteLockerListItemResponse from(FavoriteLockerListItemResult item) {
        return new FavoriteLockerListItemResponse(
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            item.isFavorite()
        );
    }
}
