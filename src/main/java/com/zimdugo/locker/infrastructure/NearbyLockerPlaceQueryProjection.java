package com.zimdugo.locker.infrastructure;

import com.zimdugo.locker.domain.NearbyLocker;
import java.time.LocalDateTime;

public interface NearbyLockerPlaceQueryProjection {
    Long getLockerId();
    String getLockerName();
    String getRoadAddress();
    double getLockerLatitude();
    double getLockerLongitude();
    double getDistanceMeters();
    String getLockerType();
    LocalDateTime getUpdatedAt();
    Boolean getIsFavorite();
    Long getPlaceId();
    String getPlaceName();

    default NearbyLocker toDomain() {
        return new NearbyLocker(
            getLockerId(),
            getLockerName(),
            getRoadAddress(),
            getLockerLatitude(),
            getLockerLongitude(),
            getDistanceMeters(),
            getLockerType(),
            getUpdatedAt(),
            getIsFavorite(),
            getPlaceId(),
            getPlaceName(),
            0
        );
    }
}
