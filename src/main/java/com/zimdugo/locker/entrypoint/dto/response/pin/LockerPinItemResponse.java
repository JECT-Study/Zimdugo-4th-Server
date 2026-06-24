package com.zimdugo.locker.entrypoint.dto.response.pin;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;

public record LockerPinItemResponse(
    LockerPinTypeResponse pinType,
    Long placeId,
    Long lockerId,
    double latitude,
    double longitude,
    Boolean isFavorite,
    Integer lockerCount
) {
    public static LockerPinItemResponse from(LockerPinItemResult item) {
        return new LockerPinItemResponse(
            LockerPinTypeResponse.from(item.pinType()),
            item.placeId(),
            item.lockerId(),
            item.latitude(),
            item.longitude(),
            item.isFavorite(),
            item.lockerCount()
        );
    }
}
