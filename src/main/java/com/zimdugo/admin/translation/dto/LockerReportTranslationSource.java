package com.zimdugo.admin.translation.dto;

import com.zimdugo.locker.infrastructure.persistence.LockerReportEntity;
import java.util.stream.Collectors;

public record LockerReportTranslationSource(
    Long reportId,
    String name,
    String roadAddress,
    String groundLevelType,
    Integer floor,
    String indoorOutdoorType,
    String lockerType,
    String lockerSize,
    String priceType,
    Integer minPrice,
    Integer maxPrice,
    String additionalInfo,
    String operatingTimeType,
    String startTime,
    String endTime
) {
    public static LockerReportTranslationSource from(LockerReportEntity report) {
        return new LockerReportTranslationSource(
            report.getId(),
            report.getName(),
            report.getRoadAddress(),
            report.getGroundLevelType() == null ? null : report.getGroundLevelType().name(),
            report.getFloor(),
            report.getIndoorOutdoorType().name(),
            report.getLockerType().name(),
            report.getLockerSize().stream()
                .map(Enum::name)
                .sorted()
                .collect(Collectors.joining(", ")),
            report.getPriceType().name(),
            report.getMinPrice(),
            report.getMaxPrice(),
            report.getAdditionalInfo(),
            report.getOperatingTimeType().name(),
            report.getStartTime() == null ? null : report.getStartTime().toString(),
            report.getEndTime() == null ? null : report.getEndTime().toString()
        );
    }
}
