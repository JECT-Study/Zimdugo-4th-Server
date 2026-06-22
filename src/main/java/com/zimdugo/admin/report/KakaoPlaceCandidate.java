package com.zimdugo.admin.report;

public record KakaoPlaceCandidate(
    String id,
    String name,
    String category,
    String roadAddress,
    double latitude,
    double longitude,
    int distanceMeters,
    String placeUrl
) {
}
