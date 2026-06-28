package com.zimdugo.locker.domain.locker;

import com.zimdugo.locker.domain.search.LockerSearchFilter;
import java.util.List;

public interface NearbyLockerPlaceReader {
    List<NearbyLocker> findWithinBounds(
        double swLat,
        double swLng,
        double neLat,
        double neLng,
        LockerSearchFilter filter
    );
}
