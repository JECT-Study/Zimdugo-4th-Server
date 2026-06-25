package com.zimdugo.admin.locker.dto;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import java.time.LocalTime;
import java.util.Set;

public record AdminLockerCommand(
    String name,
    String roadAddress,
    double latitude,
    double longitude,
    Long placeId,
    LockerType lockerType,
    IndoorOutdoorType indoorOutdoorType,
    GroundLevelType groundLevelType,
    Integer floor,
    Integer minPrice,
    Integer maxPrice,
    Set<LockerSizeType> lockerSizes,
    String detailInfo,
    LocalTime startTime,
    LocalTime endTime,
    String imageUrl
) {
}
