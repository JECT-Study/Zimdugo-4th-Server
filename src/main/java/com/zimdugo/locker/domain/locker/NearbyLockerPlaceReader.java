package com.zimdugo.locker.domain.locker;

import java.util.List;

public interface NearbyLockerPlaceReader {
    List<NearbyLocker> findWithinBounds(double swLat, double swLng, double neLat, double neLng);
}
