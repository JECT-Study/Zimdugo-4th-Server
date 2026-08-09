package com.zimdugo.locker.domain.search;

import com.zimdugo.locker.domain.locker.LockerType;
import java.time.LocalDateTime;
import java.util.Set;

public record LockerSearchCandidate(
    Long lockerId,
    String lockerName,
    String roadAddress,
    LockerType lockerType,
    Integer minPrice,
    LocalDateTime updatedAt,
    Long placeId,
    String placeName,
    Set<LockerSearchMatchSource> matchSources,
    int lockerCount,
    long distanceMeters,
    double lockerLatitude,
    double lockerLongitude,
    double placeLatitude,
    double placeLongitude,
    float score
) {
}
