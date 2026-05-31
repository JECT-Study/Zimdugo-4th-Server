package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.NearbyLocker;

public interface NearbyLockerPlaceQueryProjection {
    Long getLockerId();
    double getLockerLatitude();
    double getLockerLongitude();
    Long getPlaceId();

    default NearbyLocker toDomain() {
        return new NearbyLocker(
            getLockerId(),
            getLockerLatitude(),
            getLockerLongitude(),
            getPlaceId()
        );
    }
}
