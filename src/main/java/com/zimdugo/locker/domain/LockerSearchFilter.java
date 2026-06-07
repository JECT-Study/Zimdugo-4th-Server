package com.zimdugo.locker.domain;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record LockerSearchFilter(
    Set<LockerSizeType> sizeTypes,
    IndoorOutdoorType indoorOutdoorType,
    LockerType lockerType
) {

    public LockerSearchFilter {
        sizeTypes = sizeTypes == null || sizeTypes.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(EnumSet.copyOf(sizeTypes));
    }

    public static LockerSearchFilter empty() {
        return new LockerSearchFilter(Set.of(), null, null);
    }

    public static LockerSearchFilter from(
        Set<String> sizeTypes,
        String indoorOutdoorType,
        String lockerType
    ) {
        Set<LockerSizeType> parsedSizeTypes = sizeTypes == null
            ? Set.of()
            : sizeTypes.stream()
                .filter(Objects::nonNull)
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(LockerSizeType::from)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        return new LockerSearchFilter(
            parsedSizeTypes,
            parse(indoorOutdoorType, IndoorOutdoorType.class),
            parse(lockerType, LockerType.class)
        );
    }

    public boolean isEmpty() {
        return sizeTypes.isEmpty() && indoorOutdoorType == null && lockerType == null;
    }

    public boolean matches(
        LockerSizeType lockerSize,
        IndoorOutdoorType lockerIndoorOutdoorType,
        LockerType actualLockerType
    ) {
        return (sizeTypes.isEmpty() || sizeTypes.contains(lockerSize))
            && (indoorOutdoorType == null || indoorOutdoorType == lockerIndoorOutdoorType)
            && (lockerType == null || lockerType == actualLockerType);
    }

    private static <T extends Enum<T>> T parse(String value, Class<T> enumType) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER_FORMAT);
        }
    }
}
