package com.zimdugo.locker.domain.realtime;

public record LockerRealtimeAvailabilitySnapshot(
    String externalLockerId,
    int smallAvailableCount,
    int mediumAvailableCount,
    int largeAvailableCount
) {
}
