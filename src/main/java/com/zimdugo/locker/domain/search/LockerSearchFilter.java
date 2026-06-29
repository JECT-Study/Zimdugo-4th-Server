package com.zimdugo.locker.domain.search;

import com.zimdugo.locker.domain.locker.IndoorOutdoorType;
import com.zimdugo.locker.domain.locker.LockerSizeType;
import com.zimdugo.locker.domain.locker.LockerType;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
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

    public boolean isEmpty() {
        return sizeTypes.isEmpty() && indoorOutdoorTypes.isEmpty() && lockerTypes.isEmpty();
    }

    public boolean matches(
        Set<LockerSizeType> lockerSizes,
        IndoorOutdoorType lockerIndoorOutdoorType,
        LockerType actualLockerType
    ) {
        return (sizeTypes.isEmpty() || lockerSizes.stream().anyMatch(sizeTypes::contains))
            && (indoorOutdoorTypes.isEmpty() || indoorOutdoorTypes.contains(lockerIndoorOutdoorType))
            && (lockerTypes.isEmpty() || lockerTypes.contains(actualLockerType));
    }
}
