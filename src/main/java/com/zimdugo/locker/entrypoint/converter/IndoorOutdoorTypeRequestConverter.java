package com.zimdugo.locker.entrypoint.converter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.application.filter.IndoorOutdoorFilterType;
import java.util.Locale;
import org.springframework.core.convert.converter.Converter;

public class IndoorOutdoorTypeRequestConverter implements Converter<String, IndoorOutdoorFilterType> {

    @Override
    public IndoorOutdoorFilterType convert(String source) {
        String normalized = LockerFilterRequestValueNormalizer.normalize(source);
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        try {
            return IndoorOutdoorFilterType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }
}
