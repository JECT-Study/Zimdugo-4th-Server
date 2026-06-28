package com.zimdugo.locker.entrypoint.dto.response.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.search.LockerSearchLockerResult;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LockerSearchLockerResponse(
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
    public static LockerSearchLockerResponse from(LockerSearchLockerResult item) {
        return new LockerSearchLockerResponse(
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
