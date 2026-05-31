package com.zimdugo.locker.domain;

import java.time.LocalDateTime;

public record LockerSuggestCandidate(
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    LocalDateTime updatedAt,
    Long placeId,
    String placeName,
    int lockerCount,
    long distanceMeters,
    float score
) {
}
