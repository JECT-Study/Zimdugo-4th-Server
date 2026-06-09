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
    Set<IndoorOutdoorType> indoorOutdoorTypes,
    Set<LockerType> lockerTypes
) {

    public LockerSearchFilter {
        sizeTypes = sizeTypes == null || sizeTypes.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(EnumSet.copyOf(sizeTypes));
        indoorOutdoorTypes = indoorOutdoorTypes == null || indoorOutdoorTypes.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(EnumSet.copyOf(indoorOutdoorTypes));
        lockerTypes = lockerTypes == null || lockerTypes.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(EnumSet.copyOf(lockerTypes));
    }

    public static LockerSearchFilter empty() {
        return new LockerSearchFilter(Set.of(), Set.of(), Set.of());
    }

    public static LockerSearchFilter from(
        Set<String> sizeTypes,
        Set<String> indoorOutdoorTypes,
        Set<String> lockerTypes
    ) {
        Set<LockerSizeType> parsedSizeTypes = parseEnumSet(sizeTypes, LockerSizeType::from);
        Set<IndoorOutdoorType> parsedIndoorOutdoorTypes = parseEnumSet(indoorOutdoorTypes, val -> parse(val, IndoorOutdoorType.class));
        Set<LockerType> parsedLockerTypes = parseEnumSet(lockerTypes, val -> parse(val, LockerType.class));

        return new LockerSearchFilter(
            parsedSizeTypes,
            parsedIndoorOutdoorTypes,
            parsedLockerTypes
        );
    }

    public boolean isEmpty() {
        return sizeTypes.isEmpty() && indoorOutdoorTypes.isEmpty() && lockerTypes.isEmpty();
    }

    public boolean matches(
        LockerSizeType lockerSize,
        IndoorOutdoorType lockerIndoorOutdoorType,
        LockerType actualLockerType
    ) {
        return (sizeTypes.isEmpty() || sizeTypes.contains(lockerSize))
            && (indoorOutdoorTypes.isEmpty() || indoorOutdoorTypes.contains(lockerIndoorOutdoorType))
            && (lockerTypes.isEmpty() || lockerTypes.contains(actualLockerType));
    }

    private static <T extends Enum<T>> Set<T> parseEnumSet(Set<String> values, java.util.function.Function<String, T> mapper) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
            .filter(Objects::nonNull)
            .flatMap(value -> Arrays.stream(value.split(",")))
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());
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
