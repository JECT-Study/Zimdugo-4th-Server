package com.zimdugo.locker.application.place;

import java.util.Set;

public record PlaceLockerQueryCommand(
    Long placeId,
    double latitude,
    double longitude,
    Set<String> sizeTypes,
    Set<String> indoorOutdoorTypes,
    Set<String> lockerTypes
) {
}
