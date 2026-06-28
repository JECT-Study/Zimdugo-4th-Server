package com.zimdugo.locker.application.search;

import java.util.Set;

public record LockerSearchCommand(
    double latitude,
    double longitude,
    String keyword,
    Set<String> sizeTypes,
    Set<String> indoorOutdoorTypes,
    Set<String> lockerTypes,
    Double zoom
) {
}
