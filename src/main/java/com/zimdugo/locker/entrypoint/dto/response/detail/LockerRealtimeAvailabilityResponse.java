package com.zimdugo.locker.entrypoint.dto.response.detail;

import com.zimdugo.locker.application.result.detail.LockerRealtimeAvailabilityResult;
import java.time.LocalDateTime;

public record LockerRealtimeAvailabilityResponse(
    boolean isAvailable,
    int smallAvailableCount,
    int mediumAvailableCount,
    int largeAvailableCount,
    LocalDateTime fetchedAt
) {
    static LockerRealtimeAvailabilityResponse from(LockerRealtimeAvailabilityResult result) {
        if (result == null) {
            return null;
        }
        return new LockerRealtimeAvailabilityResponse(
            result.isAvailable(), result.smallAvailableCount(), result.mediumAvailableCount(),
            result.largeAvailableCount(), result.fetchedAt()
        );
    }
}
