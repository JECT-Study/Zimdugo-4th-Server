package com.zimdugo.locker.application.result.search;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import java.time.LocalDateTime;
import java.util.List;

public record LockerSearchItemResult(
    LockerItemType type,
    Long placeId,
    String placeName,
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    Integer minPrice,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt,
    Boolean isFavorite,
    List<LockerSearchLockerResult> lockers
) {
    public static LockerSearchItemResult locker(LockerSuggestItemResult item, boolean isFavorite) {
        return new LockerSearchItemResult(
            item.type(),
            item.placeId(),
            item.placeName(),
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.minPrice(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            isFavorite,
            List.of()
        );
    }

    public static LockerSearchItemResult place(
        LockerSuggestItemResult item,
        List<LockerSearchLockerResult> lockers
    ) {
        return new LockerSearchItemResult(
            item.type(),
            item.placeId(),
            item.placeName(),
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            null,
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            null,
            lockers
        );
    }
}
