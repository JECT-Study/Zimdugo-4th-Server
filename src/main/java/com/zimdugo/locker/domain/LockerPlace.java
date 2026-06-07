package com.zimdugo.locker.domain;

public record LockerPlace(
    Long placeId,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude
) {
}
