package com.zimdugo.locker.entrypoint.converter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.filter.LockerFacilityFilterType;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;

public class LockerTypeRequestConverter implements Converter<String, LockerFacilityFilterType> {

    @Override
    public LockerFacilityFilterType convert(String source) {
        String normalized = LockerFilterRequestValueNormalizer.normalize(source);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        try {
            return LockerFacilityFilterType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }
}
