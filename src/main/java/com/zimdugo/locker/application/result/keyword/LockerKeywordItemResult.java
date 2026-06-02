package com.zimdugo.locker.application.result.keyword;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import com.zimdugo.locker.application.result.suggest.LockerSuggestType;
import java.time.LocalDateTime;
import java.util.List;

public record LockerKeywordItemResult(
    LockerSuggestType suggestType,
    Long placeId,
    String placeName,
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt,
    Boolean isFavorite,
    List<LockerKeywordLockerResult> lockers
) {
    public static LockerKeywordItemResult locker(LockerSuggestItemResult item) {
        return new LockerKeywordItemResult(
            item.suggestType(),
            item.placeId(),
            item.placeName(),
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            false,
            List.of()
        );
    }

    public static LockerKeywordItemResult place(
        LockerSuggestItemResult item,
        List<LockerKeywordLockerResult> lockers
    ) {
        return new LockerKeywordItemResult(
            item.suggestType(),
            item.placeId(),
            item.placeName(),
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            null,
            lockers
        );
    }
}
