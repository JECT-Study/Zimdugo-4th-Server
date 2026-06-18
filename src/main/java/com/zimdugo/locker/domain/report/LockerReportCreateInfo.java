package com.zimdugo.locker.domain.report;

import java.time.LocalTime;
import java.util.List;

public record LockerReportCreateInfo(
    Long userId,
    String roadAddress,
    String groundLevelType,
    Integer floorNumber,
    String indoorOutdoorType,
    String lockerType,
    List<String> sizeTypes,
    String priceType,
    Integer minPrice,
    Integer maxPrice,
    String operatingTimeType,
    LocalTime startTime,
    LocalTime endTime,
    String additionalInfo,
    String imageUrl,
    boolean locationConsentAgreed,
    double latitude,
    double longitude
) {
}
