package com.zimdugo.locker.application.result.place;

import com.zimdugo.locker.application.result.GeoBoundsUtils;
import com.zimdugo.locker.application.result.LockerBoundsResult;
import com.zimdugo.locker.application.result.keyword.LockerKeywordLockerResult;
import com.zimdugo.locker.domain.LockerPlace;
import java.util.List;

public record PlaceLockerResult(
    Long placeId,
    String placeName,
    String roadAddress,
    double latitude,
    double longitude,
    LockerBoundsResult bounds,
    List<LockerKeywordLockerResult> lockers
) {
    public static PlaceLockerResult of(LockerPlace place, List<LockerKeywordLockerResult> lockers) {
        LockerBoundsResult bounds = GeoBoundsUtils.from(
            lockers,
            LockerKeywordLockerResult::latitude,
            LockerKeywordLockerResult::longitude
        ).orElse(null);
        return new PlaceLockerResult(
            place.placeId(),
            place.placeName(),
            place.roadAddress(),
            place.latitude(),
            place.longitude(),
            bounds,
            lockers
        );
    }
}
