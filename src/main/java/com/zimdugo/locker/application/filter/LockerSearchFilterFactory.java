package com.zimdugo.locker.application.filter;

import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class LockerSearchFilterFactory {

    private LockerSearchFilterFactory() {
    }

    public static LockerSearchFilter create(
        Set<LockerSizeFilterType> sizeTypes,
        Set<IndoorOutdoorFilterType> indoorOutdoorTypes,
        Set<LockerFacilityFilterType> lockerTypes
    ) {
        return new LockerSearchFilter(
            sizeTypes == null
                ? Set.of()
                : sizeTypes.stream()
                    .filter(Objects::nonNull)
                    .map(LockerSizeFilterType::toDomain)
                    .collect(Collectors.toUnmodifiableSet()),
            indoorOutdoorTypes == null
                ? Set.of()
                : indoorOutdoorTypes.stream()
                    .filter(Objects::nonNull)
                    .map(IndoorOutdoorFilterType::toDomain)
                    .collect(Collectors.toUnmodifiableSet()),
            lockerTypes == null
                ? Set.of()
                : lockerTypes.stream()
                    .filter(Objects::nonNull)
                    .map(LockerFacilityFilterType::toDomain)
                    .collect(Collectors.toUnmodifiableSet())
        );
    }
}
