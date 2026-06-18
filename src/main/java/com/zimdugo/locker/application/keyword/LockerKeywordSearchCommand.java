package com.zimdugo.locker.application.keyword;

import java.util.Set;

public record LockerKeywordSearchCommand(
    double latitude,
    double longitude,
    String keyword,
    Set<String> sizeTypes,
    Set<String> indoorOutdoorTypes,
    Set<String> lockerTypes
) {
}
