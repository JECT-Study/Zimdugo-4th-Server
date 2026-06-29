package com.zimdugo.locker.application.pin;

import com.zimdugo.locker.application.filter.IndoorOutdoorFilterType;
import com.zimdugo.locker.application.filter.LockerFacilityFilterType;
import com.zimdugo.locker.application.filter.LockerSizeFilterType;
import java.util.Set;

public record LockerPinQuery(
    double swLat,
    double swLng,
    double neLat,
    double neLng,
    double zoomLevel,
    Double userLat,
    Double userLng,
    String keyword,
    Set<LockerSizeFilterType> sizeTypes,
    Set<IndoorOutdoorFilterType> indoorOutdoorTypes,
    Set<LockerFacilityFilterType> lockerTypes
) {
    public LockerPinQuery(
        double swLat,
        double swLng,
        double neLat,
        double neLng,
        double zoomLevel
    ) {
        this(swLat, swLng, neLat, neLng, zoomLevel, null, null, null, null, null, null);
    }

    public boolean hasKeywordSearch() {
        return keyword != null && !keyword.isBlank();
    }
}
