package com.zimdugo.locker.domain;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.Locale;

public enum LockerSizeType {
    SMALL,
    MEDIUM,
    LARGE;

    public static LockerSizeType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);

        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_SIZE_TYPE);
        }
    }
}
