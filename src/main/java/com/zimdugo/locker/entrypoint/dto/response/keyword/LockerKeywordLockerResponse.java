package com.zimdugo.locker.entrypoint.dto.response.keyword;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.keyword.LockerKeywordLockerResult;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LockerKeywordLockerResponse(
    Long lockerId,
    String lockerName,
    String roadAddress,
    String lockerType,
    Integer minPrice,
    double latitude,
    double longitude,
    long distanceMeters,
    LocalDateTime updatedAt,
    boolean isFavorite
) {
    public static LockerKeywordLockerResponse from(LockerKeywordLockerResult item) {
        return new LockerKeywordLockerResponse(
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.minPrice(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            item.isFavorite()
        );
    }
}
