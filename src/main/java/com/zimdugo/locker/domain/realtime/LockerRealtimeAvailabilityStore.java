package com.zimdugo.locker.domain.realtime;

import java.time.LocalDateTime;
import java.util.Collection;

public interface LockerRealtimeAvailabilityStore {
    int saveMapped(Collection<LockerRealtimeAvailabilitySnapshot> snapshots, LocalDateTime fetchedAt);
}
