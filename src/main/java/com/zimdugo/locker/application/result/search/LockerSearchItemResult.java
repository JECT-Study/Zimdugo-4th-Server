package com.zimdugo.locker.application.result.search;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.application.search.LockerSearchTarget;
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
    public static LockerSearchItemResult locker(LockerSearchTarget target, boolean isFavorite) {
        return new LockerSearchItemResult(
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
            target.updatedAt(),
            isFavorite,
            List.of()
        );
    }

    public static LockerSearchItemResult place(
        LockerSearchTarget target,
        List<LockerSearchLockerResult> lockers
    ) {
        return new LockerSearchItemResult(
            target.type(),
            target.placeId(),
            target.placeName(),
            target.lockerId(),
            target.lockerName(),
            target.roadAddress(),
            target.lockerType(),
            null,
            target.latitude(),
            target.longitude(),
            target.distanceMeters(),
            target.updatedAt(),
            null,
            lockers
        );
    }
}
