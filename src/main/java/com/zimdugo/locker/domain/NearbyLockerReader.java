package com.zimdugo.locker.domain;

import java.util.List;

public interface NearbyLockerReader {
    List<NearbyLocker> findNearby(double latitude, double longitude, int radiusMeters);
}
