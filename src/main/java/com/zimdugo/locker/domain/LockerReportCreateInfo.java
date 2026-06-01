package com.zimdugo.locker.domain;

import com.zimdugo.locker.infrastructure.persistence.GroundLevelType;
import com.zimdugo.locker.infrastructure.persistence.IndoorOutdoorType;
import com.zimdugo.locker.infrastructure.persistence.LockerSizeType;
import com.zimdugo.locker.infrastructure.persistence.LockerType;
import java.time.LocalTime;
import java.util.Set;

public record LockerReportCreateInfo(
    Long userId,
    String name,
    String roadAddress,
    GroundLevelType groundLevelType,
    Integer floorNumber,
    IndoorOutdoorType indoorOutdoorType,
    LockerType lockerType,
    Set<LockerSizeType> lockerSize,
    Boolean isFree,
    Integer minPrice,
    Integer maxPrice,
    LocalTime startTime,
    LocalTime endTime,
    String additionalInfo,
    String imageUrl,
    boolean locationConsentAgreed,
    double latitude,
    double longitude
) {
}
