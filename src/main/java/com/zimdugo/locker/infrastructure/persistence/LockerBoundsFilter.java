package com.zimdugo.locker.infrastructure.persistence;

public record LockerBoundsFilter(
    boolean hasSizeTypes,
    String sizeTypes,
    boolean hasIndoorOutdoorTypes,
    String indoorOutdoorTypes,
    boolean hasLockerTypes,
    String lockerTypes
) {
}
