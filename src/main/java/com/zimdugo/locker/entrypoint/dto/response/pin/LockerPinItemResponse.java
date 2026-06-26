package com.zimdugo.locker.entrypoint.dto.response.pin;

import com.zimdugo.locker.application.result.pin.LockerPinItemResult;
import com.zimdugo.locker.entrypoint.dto.response.LockerBoundsResponse;

public record LockerPinItemResponse(
    LockerPinTypeResponse pinType,
    Long placeId,
    Long lockerId,
    double latitude,
    double longitude,
    Boolean isFavorite,
    Integer lockerCount,
    Integer pinCount,
    LockerBoundsResponse bounds
) {
    public static LockerPinItemResponse from(LockerPinItemResult item) {
        return new LockerPinItemResponse(
            LockerPinTypeResponse.from(item.pinType()),
            item.placeId(),
            item.lockerId(),
            item.latitude(),
            item.longitude(),
            item.isFavorite(),
            item.lockerCount(),
            item.pinCount(),
            LockerBoundsResponse.from(item.bounds())
        );
    }
}
