package com.zimdugo.locker.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;

final class UserLocationResolver {

    private static final double DEFAULT_LATITUDE = 37.498095;
    private static final double DEFAULT_LONGITUDE = 127.027610;

    private UserLocationResolver() {
    }

    static ResolvedLocation resolve(Double latitude, Double longitude) {
        validate(latitude, longitude);

        return new ResolvedLocation(
            latitude == null ? DEFAULT_LATITUDE : latitude,
            longitude == null ? DEFAULT_LONGITUDE : longitude
        );
    }

    private static void validate(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }

    record ResolvedLocation(double latitude, double longitude) {
    }
}
