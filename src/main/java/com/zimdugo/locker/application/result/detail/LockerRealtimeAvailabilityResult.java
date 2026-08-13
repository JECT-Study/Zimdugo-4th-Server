package com.zimdugo.locker.application.result.detail;

import com.zimdugo.locker.domain.detail.LockerRealtimeAvailability;
import java.time.LocalDateTime;

public record LockerRealtimeAvailabilityResult(
    boolean isAvailable,
    int smallAvailableCount,
    int mediumAvailableCount,
    int largeAvailableCount,
    LocalDateTime fetchedAt
) {
    static LockerRealtimeAvailabilityResult from(LockerRealtimeAvailability availability) {
        if (availability == null) {
            return null;
        }
        return new LockerRealtimeAvailabilityResult(
            availability.isAvailable(), availability.smallAvailableCount(), availability.mediumAvailableCount(),
            availability.largeAvailableCount(), availability.fetchedAt()
        );
    }
}
