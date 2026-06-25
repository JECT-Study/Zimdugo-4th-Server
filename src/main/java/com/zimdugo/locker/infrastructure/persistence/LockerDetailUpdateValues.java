package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import java.time.LocalTime;
import java.util.Set;

public record LockerDetailUpdateValues(
    LockerType lockerType,
    IndoorOutdoorType indoorOutdoorType,
    GroundLevelType groundLevelType,
    Integer floor,
    Integer minPrice,
    Integer maxPrice,
    Set<LockerSizeType> lockerSize,
    String detailInfo,
    LocalTime startTime,
    LocalTime endTime,
    String imageUrl
) {
}
