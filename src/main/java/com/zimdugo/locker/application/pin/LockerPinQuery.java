package com.zimdugo.locker.application.pin;

import java.util.Set;

public record LockerPinQuery(
    double swLat,
    double swLng,
    double neLat,
    double neLng,
    double zoomLevel,
    Double latitude,
    Double longitude,
    String keyword,
    Set<String> sizeTypes,
    Set<String> indoorOutdoorTypes,
    Set<String> lockerTypes
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
