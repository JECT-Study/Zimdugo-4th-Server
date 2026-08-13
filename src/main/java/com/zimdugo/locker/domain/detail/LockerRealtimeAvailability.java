package com.zimdugo.locker.domain.detail;

import java.time.LocalDateTime;

public record LockerRealtimeAvailability(
    int smallAvailableCount,
    int mediumAvailableCount,
    int largeAvailableCount,
    LocalDateTime fetchedAt
) {
    public boolean isAvailable() {
        return smallAvailableCount > 0 || mediumAvailableCount > 0 || largeAvailableCount > 0;
    }
}
