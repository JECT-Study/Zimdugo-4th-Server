package com.zimdugo.locker.entrypoint.dto.response.place;

import com.zimdugo.locker.application.result.place.PlaceLockerResult;
import com.zimdugo.locker.entrypoint.dto.response.keyword.LockerKeywordLockerResponse;
import java.util.List;

public record PlaceLockerResponse(
    Long placeId,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude,
    List<LockerKeywordLockerResponse> lockers
) {
    public static PlaceLockerResponse from(PlaceLockerResult result) {
        return new PlaceLockerResponse(
            result.placeId(),
            result.placeName(),
            result.roadAddress(),
            result.latitude(),
            result.longitude(),
            result.lockers().stream()
                .map(LockerKeywordLockerResponse::from)
                .toList()
        );
    }
}
