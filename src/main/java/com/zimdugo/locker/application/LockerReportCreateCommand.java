package com.zimdugo.locker.application;

import com.zimdugo.locker.domain.DuplicateHandlingType;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
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
    private static final String LEGACY_DUPLICATE_PREFIX = "__legacy_duplicate__:";
    private static final int LEGACY_DUPLICATE_PART_COUNT = 3;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public static LockerReportCreateCommand of(
        String duplicateHandlingType,
        Long existingLockerId,
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
        return new LockerReportCreateCommand(
            name,
            roadAddress,
            latitude,
            longitude,
            floor != null && !floor.isBlank(),
            parseFloorType(floor),
            parseFloorNumber(floor),
            indoorOutdoorType,
            lockerType,
            parseSizeTypes(sizeInfo),
            parseIsFree(priceInfo),
            parseMinPrice(priceInfo),
            parseMaxPrice(priceInfo),
            parseStartTime(operatingHours),
            parseEndTime(operatingHours),
            buildLegacyAdditionalInfo(duplicateHandlingType, existingLockerId, detailLocation, buildingName),
            imageUrl,
            false
        );
    }

    public DuplicateHandlingType duplicateHandlingType() {
        if (additionalInfo != null && additionalInfo.startsWith(LEGACY_DUPLICATE_PREFIX)) {
            String[] parts = additionalInfo.split(":", LEGACY_DUPLICATE_PART_COUNT);
            if (parts.length >= 2) {
                return DuplicateHandlingType.valueOf(parts[1]);
            }
        }
        return DuplicateHandlingType.CREATE_NEW;
    }

    public Long existingLockerId() {
        if (additionalInfo != null && additionalInfo.startsWith(LEGACY_DUPLICATE_PREFIX)) {
            String[] parts = additionalInfo.split(":", LEGACY_DUPLICATE_PART_COUNT);
            if (parts.length >= LEGACY_DUPLICATE_PART_COUNT && !parts[2].isBlank()) {
                return Long.valueOf(parts[2]);
            }
        }
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

    private static String buildLegacyAdditionalInfo(
        String duplicateHandlingType,
        Long existingLockerId,
        String detailLocation,
        String buildingName
    ) {
        if (duplicateHandlingType != null && !duplicateHandlingType.isBlank()) {
            return LEGACY_DUPLICATE_PREFIX
                + duplicateHandlingType.toUpperCase(Locale.ROOT)
                + ":"
                + (existingLockerId == null ? "" : existingLockerId);
        }

        StringJoiner joiner = new StringJoiner(" / ");
        if (detailLocation != null && !detailLocation.isBlank()) {
            joiner.add(detailLocation);
        }
        if (buildingName != null && !buildingName.isBlank()) {
            joiner.add(buildingName);
        }
        return joiner.length() == 0 ? null : joiner.toString();
    }

    private static String parseFloorType(String floor) {
        if (floor == null || floor.isBlank() || !floor.contains(":")) {
            return null;
        }
        return floor.split(":", 2)[0];
    }

    private static Integer parseFloorNumber(String floor) {
        if (floor == null || floor.isBlank() || !floor.contains(":")) {
            return null;
        }
        return Integer.valueOf(floor.split(":", 2)[1]);
    }

    private static List<String> parseSizeTypes(String sizeInfo) {
        if (sizeInfo == null || sizeInfo.isBlank()) {
            return null;
        }
        return List.of(sizeInfo.split(","));
    }

    private static Boolean parseIsFree(String priceInfo) {
        if (priceInfo == null || priceInfo.isBlank()) {
            return null;
        }
        return "FREE".equalsIgnoreCase(priceInfo);
    }

    private static Integer parseMinPrice(String priceInfo) {
        if (
            priceInfo == null
                || priceInfo.isBlank()
                || "FREE".equalsIgnoreCase(priceInfo)
                || !priceInfo.contains("~")
        ) {
            return null;
        }
        String min = priceInfo.split("~", 2)[0];
        return min.isBlank() ? null : Integer.valueOf(min);
    }

    private static Integer parseMaxPrice(String priceInfo) {
        if (
            priceInfo == null
                || priceInfo.isBlank()
                || "FREE".equalsIgnoreCase(priceInfo)
                || !priceInfo.contains("~")
        ) {
            return null;
        }
        String max = priceInfo.split("~", 2)[1];
        return max.isBlank() ? null : Integer.valueOf(max);
    }

    private static LocalTime parseStartTime(String operatingHours) {
        if (operatingHours == null || operatingHours.isBlank() || !operatingHours.contains("~")) {
            return null;
        }
        return LocalTime.parse(operatingHours.split("~", 2)[0]);
    }

    private static LocalTime parseEndTime(String operatingHours) {
        if (operatingHours == null || operatingHours.isBlank() || !operatingHours.contains("~")) {
            return null;
        }
        return LocalTime.parse(operatingHours.split("~", 2)[1]);
    }
}
