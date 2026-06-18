package com.zimdugo.locker.application.result.suggest;

import com.zimdugo.locker.application.result.LockerItemType;
import com.zimdugo.locker.domain.search.LockerSuggestCandidate;
import java.time.LocalDateTime;

public record LockerSuggestItemResult(
    LockerItemType type,
    Long placeId,
    String placeName,
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    Integer minPrice,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt
) {
    public static LockerSuggestItemResult locker(LockerSuggestCandidate candidate) {
        return new LockerSuggestItemResult(
            LockerItemType.LOCKER,
            candidate.placeId(),
            candidate.placeName(),
            candidate.lockerId(),
            candidate.lockerName(),
            candidate.roadAddress(),
            candidate.lockerType().name(),
            candidate.minPrice(),
            candidate.lockerLatitude(),
            candidate.lockerLongitude(),
            candidate.distanceMeters(),
            candidate.updatedAt()
        );
    }

    public static LockerSuggestItemResult place(LockerSuggestCandidate candidate) {
        return new LockerSuggestItemResult(
            LockerItemType.PLACE,
            candidate.placeId(),
            candidate.placeName(),
            null,
            null,
            candidate.roadAddress(),
            null,
            null,
            candidate.placeLatitude(),
            candidate.placeLongitude(),
            candidate.distanceMeters(),
            null
        );
    }
}
