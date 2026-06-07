package com.zimdugo.locker.entrypoint.dto.response;

import com.zimdugo.locker.application.result.LockerItemType;

public enum LockerItemTypeResponse {
    PLACE,
    LOCKER;

    public static LockerItemTypeResponse from(LockerItemType type) {
        return LockerItemTypeResponse.valueOf(type.name());
    }
}
