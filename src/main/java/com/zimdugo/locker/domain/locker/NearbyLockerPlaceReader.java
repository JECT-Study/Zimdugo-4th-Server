package com.zimdugo.locker.domain.locker;

import java.util.List;

public interface NearbyLockerPlaceReader {
    List<NearbyLocker> findNearby(double latitude, double longitude, int radiusMeters);
}
