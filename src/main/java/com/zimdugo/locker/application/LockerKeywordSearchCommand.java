package com.zimdugo.locker.application;

import java.util.Set;

public record LockerKeywordSearchCommand(
    double latitude,
    double longitude,
    String keyword,
    int limit,
    Set<String> sizeTypes,
    String indoorOutdoorType,
    String lockerType
) {
}
