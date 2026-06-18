package com.zimdugo.locker.domain.place;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import java.time.LocalDateTime;
import java.util.Set;

public record LockerPlaceLocker(
    Long placeId,
    Long lockerId,
    String lockerName,
    String roadAddress,
    LockerType lockerType,
    IndoorOutdoorType indoorOutdoorType,
    Set<LockerSizeType> lockerSizes,
    Integer minPrice,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt
) {
}
