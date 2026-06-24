package com.zimdugo.locker.application.result.pin;

public record LockerPinItemResult(
    LockerPinType pinType,
    Long placeId,
    Long lockerId,
    double latitude,
    double longitude,
    Boolean isFavorite,
    Integer lockerCount
) {
    public static LockerPinItemResult place(Long placeId, double latitude, double longitude, int count) {
        return new LockerPinItemResult(LockerPinType.PLACE, placeId, null, latitude, longitude, null, count);
    }

    public static LockerPinItemResult locker(Long lockerId, double latitude, double longitude, boolean isFavorite) {
        return new LockerPinItemResult(LockerPinType.LOCKER, null, lockerId, latitude, longitude, isFavorite, null);
    }
}
