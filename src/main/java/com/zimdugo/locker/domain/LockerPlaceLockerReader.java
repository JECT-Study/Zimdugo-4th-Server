package com.zimdugo.locker.domain;

import java.util.List;
import java.util.Map;

public interface LockerPlaceLockerReader {
    Map<Long, List<LockerPlaceLocker>> readByPlaceIds(
        double latitude,
        double longitude,
        List<Long> placeIds,
        LockerSearchFilter filter,
        String languageCode
    );

    default Map<Long, List<LockerPlaceLocker>> readByPlaceIds(
        double latitude,
        double longitude,
        List<Long> placeIds,
        LockerSearchFilter filter
    ) {
        return readByPlaceIds(latitude, longitude, placeIds, filter, "ko");
    }
}
