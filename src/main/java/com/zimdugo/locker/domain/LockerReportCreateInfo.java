package com.zimdugo.locker.domain;

public record LockerReportCreateInfo(
    Long lockerId,
    Long userId,
    DuplicateHandlingType duplicateHandlingType,
    String name,
    String roadAddress,
    String detailLocation,
    String buildingName,
    String floor,
    String indoorOutdoorType,
    String lockerType,
    String sizeInfo,
    String priceInfo,
    String operatingHours,
    String imageUrl,
    double latitude,
    double longitude
) {
}
