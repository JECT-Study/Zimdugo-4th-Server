package com.zimdugo.locker.application.filter;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;

public enum IndoorOutdoorFilterType {
    INDOOR,
    OUTDOOR;

    public IndoorOutdoorType toDomain() {
        return IndoorOutdoorType.valueOf(name());
    }
}
