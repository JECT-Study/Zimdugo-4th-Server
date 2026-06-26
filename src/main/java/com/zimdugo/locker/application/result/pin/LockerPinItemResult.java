package com.zimdugo.locker.application.result.pin;

import com.zimdugo.locker.application.result.LockerBoundsResult;

public record LockerPinItemResult(
    LockerPinType pinType,
    Long placeId,
    Long lockerId,
    double latitude,
    double longitude,
    Boolean isFavorite,
    Integer lockerCount,
    Integer pinCount,
    LockerBoundsResult bounds
) {
    public static LockerPinItemResult place(Long placeId, double latitude, double longitude, int count) {
        return new LockerPinItemResult(
            LockerPinType.PLACE,
            placeId,
            null,
            latitude,
            longitude,
            null,
            count,
            null,
            null
        );
    }

    public static LockerPinItemResult locker(Long lockerId, double latitude, double longitude, boolean isFavorite) {
        return new LockerPinItemResult(
            LockerPinType.LOCKER,
            null,
            lockerId,
            latitude,
            longitude,
            isFavorite,
            null,
            null,
            null
        );
    }

    public static LockerPinItemResult cluster(
        double latitude,
        double longitude,
        int pinCount,
        LockerBoundsResult bounds
    ) {
        return new LockerPinItemResult(
            LockerPinType.CLUSTER,
            null,
            null,
            latitude,
            longitude,
            null,
            null,
            pinCount,
            bounds
        );
    }
}
