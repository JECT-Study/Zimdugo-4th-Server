package com.zimdugo.locker.application.filter;

import com.zimdugo.locker.domain.locker.LockerSizeType;

public enum LockerSizeFilterType {
    SMALL,
    MEDIUM,
    LARGE;

    public LockerSizeType toDomain() {
        return LockerSizeType.valueOf(name());
    }
}
