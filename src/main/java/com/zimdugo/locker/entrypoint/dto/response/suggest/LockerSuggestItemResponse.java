package com.zimdugo.locker.entrypoint.dto.response.suggest;

import com.zimdugo.locker.application.result.suggest.LockerSuggestItemResult;
import java.time.LocalDateTime;

public record LockerSuggestItemResponse(
    LockerSuggestTypeResponse suggestType,
    Long placeId,
    String placeName,
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    long distanceMeters,
    LocalDateTime updatedAt
) {
    public static LockerSuggestItemResponse from(LockerSuggestItemResult item) {
        return new LockerSuggestItemResponse(
            LockerSuggestTypeResponse.from(item.suggestType()),
            item.placeId(),
            item.placeName(),
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.distanceMeters(),
            item.updatedAt()
        );
    }
}
