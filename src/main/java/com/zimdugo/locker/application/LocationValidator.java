package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;

final class LocationValidator {

    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    private LocationValidator() {
    }

    static void validate(double latitude, double longitude) {
        if (latitude < MIN_LATITUDE
            || latitude > MAX_LATITUDE
            || longitude < MIN_LONGITUDE
            || longitude > MAX_LONGITUDE) {
            throw new BusinessException(ErrorCode.INVALID_LOCATION_RANGE);
        }
    }
}
