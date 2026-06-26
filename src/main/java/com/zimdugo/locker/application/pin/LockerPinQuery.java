package com.zimdugo.locker.application.pin;

public record LockerPinQuery(
    double swLat,
    double swLng,
    double neLat,
    double neLng,
    double zoomLevel
) {
}
