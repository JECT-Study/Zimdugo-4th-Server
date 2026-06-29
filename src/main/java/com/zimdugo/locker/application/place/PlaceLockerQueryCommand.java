package com.zimdugo.locker.application.place;

import com.zimdugo.locker.application.filter.IndoorOutdoorFilterType;
import com.zimdugo.locker.application.filter.LockerFacilityFilterType;
import com.zimdugo.locker.application.filter.LockerSizeFilterType;
import java.util.Set;

public record PlaceLockerQueryCommand(
    Long placeId,
    double latitude,
    double longitude,
    Set<LockerSizeFilterType> sizeTypes,
    Set<IndoorOutdoorFilterType> indoorOutdoorTypes,
    Set<LockerFacilityFilterType> lockerTypes
) {
}
