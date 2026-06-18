package com.zimdugo.locker.domain.place;

import com.zimdugo.locker.domain.search.LockerSearchFilter;
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
}
