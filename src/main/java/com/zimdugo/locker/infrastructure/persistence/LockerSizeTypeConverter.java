package com.zimdugo.locker.infrastructure.persistence;

import com.zimdugo.locker.domain.locker.LockerSizeType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Converter(autoApply = false)
public class LockerSizeTypeConverter implements AttributeConverter<Set<LockerSizeType>, String> {

    @Override
    public String convertToDatabaseColumn(Set<LockerSizeType> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        return attribute.stream()
            .filter(Objects::nonNull)
            .map(Enum::name)
            .sorted()
            .collect(Collectors.joining(","));
    }

    @Override
    public Set<LockerSizeType> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptySet();
        }

        EnumSet<LockerSizeType> sizes = EnumSet.noneOf(LockerSizeType.class);
        String[] tokens = dbData.split(",");

        for (String token : tokens) {
            LockerSizeType size = LockerSizeType.from(token);
            if (size != null) {
                sizes.add(size);
            }
        }

        return Collections.unmodifiableSet(sizes);
    }
}
