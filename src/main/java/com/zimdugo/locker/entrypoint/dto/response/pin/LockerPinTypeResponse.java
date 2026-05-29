package com.zimdugo.locker.entrypoint.dto.response.pin;

import com.zimdugo.locker.application.result.pin.LockerPinType;

public enum LockerPinTypeResponse {
    PLACE,
    LOCKER;

    public static LockerPinTypeResponse from(LockerPinType type) {
        return LockerPinTypeResponse.valueOf(type.name());
    }
}
