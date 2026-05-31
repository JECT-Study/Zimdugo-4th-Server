package com.zimdugo.locker.domain;

import java.time.LocalTime;
import java.util.List;

public record LockerReportCreateInfo(
    Long userId,
    String name,
    String roadAddress,
    boolean hasFloor,
    String floorType,
    Integer floorNumber,
    String indoorOutdoorType,
    String lockerType,
    List<String> sizeTypes,
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
