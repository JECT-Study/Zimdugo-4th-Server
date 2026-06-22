package com.zimdugo.admin.report.dto;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;

public record AdminLockerReportApprovalCommand(
    Long existingPlaceId,
    String kakaoPlaceId,
    String placeName,
    String lockerName,
    String roadAddress,
    double latitude,
    double longitude
) {
    private static final int MAX_LOCKER_NAME_LENGTH = 100;
    private static final int MAX_PLACE_NAME_LENGTH = 120;
    private static final int MAX_ADDRESS_LENGTH = 255;
    private static final int MIN_LATITUDE = -90;
    private static final int MAX_LATITUDE = 90;
    private static final int MIN_LONGITUDE = -180;
    private static final int MAX_LONGITUDE = 180;

    public AdminLockerReportApprovalCommand {
        boolean invalidPlaceName = existingPlaceId == null
            ? placeName == null || placeName.isBlank() || placeName.length() > MAX_PLACE_NAME_LENGTH
            : placeName != null && placeName.length() > MAX_PLACE_NAME_LENGTH;
        if (invalidPlaceName
            || lockerName == null || lockerName.isBlank() || lockerName.length() > MAX_LOCKER_NAME_LENGTH
            || roadAddress == null || roadAddress.isBlank() || roadAddress.length() > MAX_ADDRESS_LENGTH
            || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE
            || longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_REPORT_INPUT);
        }
    }
}
