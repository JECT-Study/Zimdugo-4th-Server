package com.zimdugo.locker.application.result.keyword;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import java.time.LocalDateTime;
import java.util.List;

public record LockerKeywordItemResult(
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
    List<LockerKeywordLockerResult> lockers
) {
    public static LockerKeywordItemResult locker(LockerSuggestItemResult item) {
        return new LockerKeywordItemResult(
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
            false,
            List.of()
        );
    }

    public static LockerKeywordItemResult place(
        LockerSuggestItemResult item,
        List<LockerKeywordLockerResult> lockers
    ) {
        return new LockerKeywordItemResult(
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
