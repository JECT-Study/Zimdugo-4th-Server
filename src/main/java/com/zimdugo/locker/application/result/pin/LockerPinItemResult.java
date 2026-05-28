package com.zimdugo.locker.application.result.pin;

public record LockerPinItemResult(
    LockerPinType pinType,
    Long placeId,
    Long lockerId,
    double latitude,
    double longitude
) {
    public static LockerPinItemResult place(Long placeId, double latitude, double longitude) {
        return new LockerPinItemResult(LockerPinType.PLACE, placeId, null, latitude, longitude);
    }

    public static LockerPinItemResult locker(Long lockerId, double latitude, double longitude) {
        return new LockerPinItemResult(LockerPinType.LOCKER, null, lockerId, latitude, longitude);
    }
}
