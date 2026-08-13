package com.zimdugo.locker.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LockerRealtimeAvailabilityEntityTest {

    @Test
    void assignedIdEntityIsNewUntilPersisted() {
        LockerRealtimeAvailabilityEntity entity = new LockerRealtimeAvailabilityEntity(
            "TL1",
            3,
            2,
            1,
            LocalDateTime.of(2026, 8, 13, 10, 0)
        );

        assertThat(entity.isNew()).isTrue();

        entity.markPersisted();

        assertThat(entity.isNew()).isFalse();
    }
}
