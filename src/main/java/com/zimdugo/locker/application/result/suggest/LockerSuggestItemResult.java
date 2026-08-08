package com.zimdugo.locker.application.result.suggest;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.search.LockerSearchTarget;
import java.time.LocalDateTime;

public record LockerSuggestItemResult(
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
    LocalDateTime updatedAt
) {
    public static LockerSuggestItemResult from(LockerSearchTarget target) {
        return new LockerSuggestItemResult(
            target.type(),
            target.placeId(),
            target.placeName(),
            target.lockerId(),
            target.lockerName(),
            target.roadAddress(),
            target.lockerType(),
            target.minPrice(),
            target.latitude(),
            target.longitude(),
            target.distanceMeters(),
            target.updatedAt()
        );
    }
}
