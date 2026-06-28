package com.zimdugo.locker.entrypoint.dto.response.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.search.LockerSearchItemResult;
import com.zimdugo.locker.entrypoint.dto.response.LockerItemTypeResponse;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LockerSearchItemResponse(
    LockerItemTypeResponse type,
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
    LocalDateTime updatedAt,
    Boolean isFavorite,
    List<LockerSearchLockerResponse> lockers
) {
    public static LockerSearchItemResponse from(LockerSearchItemResult item) {
        return new LockerSearchItemResponse(
            LockerItemTypeResponse.from(item.type()),
            item.placeId(),
            item.placeName(),
            item.lockerId(),
            item.lockerName(),
            item.roadAddress(),
            item.lockerType(),
            item.minPrice(),
            item.latitude(),
            item.longitude(),
            item.distanceMeters(),
            item.updatedAt(),
            item.isFavorite(),
            item.lockers().stream()
                .map(LockerSearchLockerResponse::from)
                .toList()
        );
    }
}
