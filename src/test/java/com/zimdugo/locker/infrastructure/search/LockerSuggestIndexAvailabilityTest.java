package com.zimdugo.locker.infrastructure.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;

import static org.assertj.core.api.Assertions.assertThat;

class LockerSuggestIndexAvailabilityTest {

    @Test
    @DisplayName("검색 인덱스가 확인되기 전에는 DOWN 상태다")
    void startsDownUntilServingIndexIsConfirmed() {
        LockerSuggestIndexAvailability availability = new LockerSuggestIndexAvailability();

        assertThat(availability.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("검색 가능한 alias가 확인되면 UP 상태로 전환한다")
    void becomesUpWhenServingIndexIsAvailable() {
        LockerSuggestIndexAvailability availability = new LockerSuggestIndexAvailability();

        availability.markAvailable();

        assertThat(availability.health().getStatus()).isEqualTo(Status.UP);
    }
}
