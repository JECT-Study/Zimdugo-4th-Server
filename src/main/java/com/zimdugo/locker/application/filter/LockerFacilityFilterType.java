package com.zimdugo.locker.application.filter;

import com.zimdugo.locker.domain.locker.LockerType;

public enum LockerFacilityFilterType {
    MUSEUM,
    SUBWAY_STATION,
    DEPARTMENT_STORE,
    CONVENIENCE_STORE,
    PUBLIC_OFFICE,
    PRIVATE_LOCKER,
    TRAIN_STATION,
    ETC;

    public LockerType toDomain() {
        return LockerType.valueOf(name());
    }
}
