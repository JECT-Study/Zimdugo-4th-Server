package com.zimdugo.locker.domain.realtime;

import java.util.List;

public interface LockerRealtimeAvailabilityProvider {
    List<LockerRealtimeAvailabilitySnapshot> fetchAll();
}
