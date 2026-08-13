package com.zimdugo.locker.domain.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LockerRealtimeAvailabilityTest {

    @Test
    void reportsAvailableWithoutAddingCounts() {
        LockerRealtimeAvailability availability = new LockerRealtimeAvailability(
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            0,
            LocalDateTime.of(2026, 8, 13, 10, 0)
        );

        assertThat(availability.isAvailable()).isTrue();
    }
}
