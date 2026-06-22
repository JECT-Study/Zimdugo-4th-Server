package com.zimdugo.locker.application.report;

import com.zimdugo.locker.domain.report.LockerReportCreateInfo;
import java.time.LocalTime;
import java.util.List;

public record LockerReportCreateCommand(
    String roadAddress,
    double latitude,
    double longitude,
    String floorType,
    Integer floorNumber,
    String indoorOutdoorType,
    String lockerType,
    List<String> sizeTypes,
    Integer minPrice,
    Integer maxPrice,
    LocalTime startTime,
    LocalTime endTime,
    String additionalInfo,
    String imageUrl,
    boolean locationConsentAgreed
) {
    private static final LocalTime ALL_DAY_START = LocalTime.MIDNIGHT;
    private static final LocalTime ALL_DAY_END = LocalTime.of(23, 59);

    public LockerReportCreateInfo toCreateInfo(Long userId) {
        return new LockerReportCreateInfo(
            userId,
            roadAddress,
            floorType,
            floorNumber,
            indoorOutdoorType,
            lockerType,
            sizeTypes,
            resolvePriceType(),
            minPrice,
            maxPrice,
            resolveOperatingTimeType(),
            startTime,
            endTime,
            additionalInfo,
            imageUrl,
            locationConsentAgreed,
            latitude,
            longitude
        );
    }

    private String resolvePriceType() {
        if (minPrice == null && maxPrice == null) {
            return "UNKNOWN";
        }
        if (minPrice == 0 && maxPrice == 0) {
            return "FREE";
        }
        return "PAID";
    }

    private String resolveOperatingTimeType() {
        if (startTime == null && endTime == null) {
            return "UNKNOWN";
        }
        if (startTime.equals(endTime) || startTime.equals(ALL_DAY_START) && endTime.equals(ALL_DAY_END)) {
            return "OPEN_24_HOURS";
        }
        return "TIME_RANGE";
    }

}
