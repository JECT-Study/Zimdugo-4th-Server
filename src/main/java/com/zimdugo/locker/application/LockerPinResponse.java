package com.zimdugo.locker.application;

public record LockerPinResponse(
    LockerPinType pinType,
    Long placeId,
    Long lockerId,
    double latitude,
    double longitude
) {
    public static LockerPinResponse place(Long placeId, double latitude, double longitude) {
        return new LockerPinResponse(LockerPinType.PLACE, placeId, null, latitude, longitude);
    }

    public static LockerPinResponse locker(Long lockerId, double latitude, double longitude) {
        return new LockerPinResponse(LockerPinType.LOCKER, null, lockerId, latitude, longitude);
    }
}
