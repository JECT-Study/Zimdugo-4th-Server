package com.zimdugo.locker.entrypoint.converter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.filter.LockerSizeFilterType;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;

public class LockerSizeTypeRequestConverter implements Converter<String, LockerSizeFilterType> {

    @Override
    public LockerSizeFilterType convert(String source) {
        String normalized = LockerFilterRequestValueNormalizer.normalize(source);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        try {
            return LockerSizeFilterType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_LOCKER_SIZE_TYPE);
        }
    }
}
