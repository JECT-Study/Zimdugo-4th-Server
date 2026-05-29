package com.zimdugo.locker.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class LockerSizeTypeConverter implements AttributeConverter<LockerSizeType, String> {

    @Override
    public String convertToDatabaseColumn(LockerSizeType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public LockerSizeType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String normalized = dbData.trim().toUpperCase(Locale.ROOT);
        if (normalized.contains(",")) {
            String[] tokens = normalized.split(",");
            for (String token : tokens) {
                LockerSizeType parsed = parseToken(token);
                if (parsed != null) {
                    return parsed;
                }
            }
            return null;
        }

        return parseToken(normalized);
    }

    private LockerSizeType parseToken(String token) {
        String value = token.trim();
        if (value.isEmpty()) {
            return null;
        }
        if ("LARGE".equals(value)) {
            return LockerSizeType.BIG;
        }
        try {
            return LockerSizeType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
