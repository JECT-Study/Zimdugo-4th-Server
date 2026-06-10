package com.zimdugo.locker.application.result.mypage;

import java.time.LocalTime;
import java.util.List;

public record MyLockerReportDetailResult(
    Long reportId,
    String lockerName,
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
}
