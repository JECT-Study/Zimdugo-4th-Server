package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.DuplicateHandlingType;
import java.util.Locale;

public record LockerReportCreateCommand(
    DuplicateHandlingType duplicateHandlingType,
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
            parseDuplicateHandlingType(duplicateHandlingType, existingLockerId),
            existingLockerId,
            name,
            roadAddress,
            detailLocation,
            buildingName,
            floor,
            indoorOutdoorType,
            lockerType,
            sizeInfo,
            priceInfo,
            operatingHours,
            imageUrl,
            latitude,
            longitude
        );
    }

    private static DuplicateHandlingType parseDuplicateHandlingType(String value, Long existingLockerId) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        try {
            DuplicateHandlingType duplicateHandlingType = DuplicateHandlingType.valueOf(
                value.toUpperCase(Locale.ROOT)
            );
            validateExistingLockerId(duplicateHandlingType, existingLockerId);
            return duplicateHandlingType;
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    private static void validateExistingLockerId(
        DuplicateHandlingType duplicateHandlingType,
        Long existingLockerId
    ) {
        if (duplicateHandlingType == DuplicateHandlingType.ADD_TO_EXISTING && existingLockerId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
