package com.zimdugo.locker.application.search;

import com.zimdugo.locker.application.filter.IndoorOutdoorFilterType;
import com.zimdugo.locker.application.filter.LockerFacilityFilterType;
import com.zimdugo.locker.application.filter.LockerSizeFilterType;
import java.util.Set;

public record LockerSearchCommand(
    double latitude,
    double longitude,
    String keyword,
    Set<LockerSizeFilterType> sizeTypes,
    Set<IndoorOutdoorFilterType> indoorOutdoorTypes,
    Set<LockerFacilityFilterType> lockerTypes,
    Double zoom
) {
}
