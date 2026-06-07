package com.zimdugo.locker.entrypoint.dto.response.keyword;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.keyword.LockerKeywordItemResult;
import com.zimdugo.locker.entrypoint.dto.response.suggest.LockerSuggestTypeResponse;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LockerKeywordItemResponse(
    LockerSuggestTypeResponse suggestType,
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
    List<LockerKeywordLockerResponse> lockers
) {
    public static LockerKeywordItemResponse from(LockerKeywordItemResult item) {
        return new LockerKeywordItemResponse(
            LockerSuggestTypeResponse.from(item.suggestType()),
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
                .map(LockerKeywordLockerResponse::from)
                .toList()
        );
    }
}
