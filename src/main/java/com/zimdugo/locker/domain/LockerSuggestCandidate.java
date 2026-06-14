package com.zimdugo.locker.domain;

import java.time.LocalDateTime;
import java.util.Set;

public record LockerSuggestCandidate(
    Long lockerId,
    String lockerName,
    String roadAddress,
    LockerType lockerType,
    Integer minPrice,
    LocalDateTime updatedAt,
    Long placeId,
    String placeName,
    Set<String> matchedQueries,
    int lockerCount,
    long distanceMeters,
    double lockerLatitude,
    double lockerLongitude,
    double placeLatitude,
    double placeLongitude,
    float score
) {
    public static final String PLACE_NAME_QUERY = "place_name";
    public static final String LOCKER_NAME_QUERY = "locker_name";
}
