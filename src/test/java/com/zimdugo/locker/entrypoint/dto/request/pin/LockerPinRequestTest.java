package com.zimdugo.locker.entrypoint.dto.request.pin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LockerPinRequestTest {

    @Test
    @DisplayName("사용자 기준 좌표는 키워드와 무관하게 항상 필요하다")
    void rejectsMissingLocationEvenWithoutKeyword() {
        LockerPinRequest request = new LockerPinRequest(
            37.54,
            126.92,
            37.56,
            126.94,
            13.0,
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertThat(request.hasUserLocation()).isFalse();
    }

    @Test
    @DisplayName("키워드 검색이면 기준 좌표가 없을 때도 유효하지 않다")
    void rejectsMissingKeywordLocation() {
        LockerPinRequest request = new LockerPinRequest(
            37.54,
            126.92,
            37.56,
            126.94,
            13.0,
            null,
            null,
            "station",
            null,
            null,
            null
        );

        assertThat(request.hasUserLocation()).isFalse();
    }

    @Test
    @DisplayName("사용자 기준 좌표가 모두 있으면 유효하다")
    void acceptsCompleteLocation() {
        LockerPinRequest request = new LockerPinRequest(
            37.54,
            126.92,
            37.56,
            126.94,
            13.0,
            37.55,
            126.93,
            "station",
            null,
            null,
            null
        );

        assertThat(request.hasUserLocation()).isTrue();
    }
}
