package com.zimdugo.locker.entrypoint.dto.response.place;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.zimdugo.locker.application.result.place.PlaceLockerResult;
import com.zimdugo.locker.entrypoint.dto.response.LockerBoundsResponse;
import com.zimdugo.locker.entrypoint.dto.response.keyword.LockerKeywordLockerResponse;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaceLockerResponse(
    Long placeId,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude,
    LockerBoundsResponse bounds,
    List<LockerKeywordLockerResponse> lockers
) {
    public static PlaceLockerResponse from(PlaceLockerResult result) {
        return new PlaceLockerResponse(
            result.placeId(),
            result.placeName(),
            result.roadAddress(),
            result.latitude(),
            result.longitude(),
            LockerBoundsResponse.from(result.bounds()),
            result.lockers().stream()
                .map(LockerKeywordLockerResponse::from)
                .toList()
        );
    }
}
