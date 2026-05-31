package com.zimdugo.locker.application;

import java.time.LocalTime;
import java.util.List;

public record LockerReportCreateCommand(
    String roadAddress,
    double latitude,
    double longitude,
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
    boolean locationConsentAgreed
) {
    private static final String DEFAULT_REPORT_NAME = "물품보관함";

    public String name() {
        return DEFAULT_REPORT_NAME;
    }
}
