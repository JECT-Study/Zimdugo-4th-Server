package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record LockerSuggestCandidate(
    Long lockerId,
    String lockerName,
    String roadAddress,
    LockerType lockerType,
    LocalDateTime updatedAt,
    Long placeId,
    String placeName,
    int lockerCount,
    long distanceMeters,
    double lockerLatitude,
    double lockerLongitude,
    double placeLatitude,
    double placeLongitude,
    float score
) {
}
