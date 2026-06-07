package com.zimdugo.locker.application.result.suggest;

import com.zimdugo.locker.domain.LockerSuggestCandidate;
import java.time.LocalDateTime;

public record LockerSuggestItemResult(
    LockerSuggestType suggestType,
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
            LockerSuggestType.LOCKER,
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
            LockerSuggestType.PLACE,
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
