package com.zimdugo.locker.infrastructure.projection;

import com.zimdugo.locker.domain.locker.NearbyLocker;

public interface NearbyLockerPlaceQueryProjection {
    Long getLockerId();
    double getLockerLatitude();
    double getLockerLongitude();
    Long getPlaceId();
    String getLockerType();
    String getIndoorOutdoorType();
    String getLockerSize();

    default NearbyLocker toDomain() {
        return new NearbyLocker(
            getLockerId(),
            getLockerLatitude(),
            getLockerLongitude(),
            getPlaceId()
        );
    }
}
