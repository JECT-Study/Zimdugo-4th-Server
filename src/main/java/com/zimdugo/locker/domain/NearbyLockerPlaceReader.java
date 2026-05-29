package com.zimdugo.locker.domain;

import java.util.List;

public interface NearbyLockerPlaceReader {
    List<NearbyLocker> findNearby(double latitude, double longitude, int radiusMeters);
}
