package com.zimdugo.locker.application.result.search;

import com.zimdugo.locker.domain.place.LockerPlaceLocker;
import java.time.LocalDateTime;

public record LockerSearchLockerResult(
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    Integer minPrice,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt,
    boolean isFavorite
) {
    public static LockerSearchLockerResult from(LockerPlaceLocker locker, boolean isFavorite) {
        return new LockerSearchLockerResult(
            locker.lockerId(),
            locker.lockerName(),
            locker.roadAddress(),
            locker.lockerType().name(),
            locker.minPrice(),
            locker.latitude(),
            locker.longitude(),
            locker.distanceMeters(),
            locker.updatedAt(),
            isFavorite
        );
    }
}
