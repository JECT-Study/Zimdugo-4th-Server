package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.DuplicateHandlingType;
import java.time.LocalTime;
import java.util.List;
import java.util.StringJoiner;

public record LockerReportCreateCommand(
    String reportName,
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

    public DuplicateHandlingType duplicateHandlingType() {
        return DuplicateHandlingType.CREATE_NEW;
    }

    public Long existingLockerId() {
        return null;
    }

    public String name() {
        return reportName;
    }

    public String detailLocation() {
        return null;
    }

    public String buildingName() {
        return null;
    }

    public String floor() {
        if (!hasFloor) {
            return null;
        }
        return floorType + ":" + floorNumber;
    }

    public String sizeInfo() {
        if (sizeTypes == null || sizeTypes.isEmpty()) {
            return null;
        }
        return String.join(",", sizeTypes);
    }

    public String priceInfo() {
        if (isFree == null) {
            return null;
        }
        if (Boolean.TRUE.equals(isFree)) {
            return "FREE";
        }

        StringJoiner joiner = new StringJoiner("~");
        joiner.add(minPrice == null ? "" : minPrice.toString());
        joiner.add(maxPrice == null ? "" : maxPrice.toString());
        return joiner.toString();
    }

    public String operatingHours() {
        if (startTime == null || endTime == null) {
            return null;
        }
        return startTime + "~" + endTime;
    }
}
