package com.zimdugo.locker.entrypoint.dto.response.detail;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zimdugo.common.config.JacksonConfig;
import com.zimdugo.locker.application.result.detail.LockerDetailResult;
import com.zimdugo.locker.application.result.detail.LockerRealtimeAvailabilityResult;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class LockerDetailResponseTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    void serializesRealtimeAvailabilityForMappedLocker() throws Exception {
        LocalDateTime fetchedAt = LocalDateTime.of(2026, 8, 13, 10, 0);
        LockerDetailResult result = LockerDetailResult.builder()
            .lockerId(10L)
            .lockerName("신촌역 보관함")
            .realtimeAvailability(new LockerRealtimeAvailabilityResult(true, 3, 2, 1, fetchedAt))
            .build();

        JsonNode json = objectMapper.valueToTree(LockerDetailResponse.from(result));

        assertThat(json.at("/realtimeAvailability/isAvailable").booleanValue()).isTrue();
        assertThat(json.at("/realtimeAvailability/smallAvailableCount").intValue()).isEqualTo(3);
        assertThat(json.at("/realtimeAvailability/mediumAvailableCount").intValue()).isEqualTo(2);
        assertThat(json.at("/realtimeAvailability/largeAvailableCount").intValue()).isEqualTo(1);
        assertThat(json.at("/realtimeAvailability/fetchedAt").textValue()).isEqualTo("2026-08-13T10:00:00");
    }

    @Test
    void omitsRealtimeAvailabilityForUnmappedLocker() {
        LockerDetailResult result = LockerDetailResult.builder()
            .lockerId(10L)
            .lockerName("신촌역 보관함")
            .build();

        JsonNode json = objectMapper.valueToTree(LockerDetailResponse.from(result));

        assertThat(json.has("realtimeAvailability")).isFalse();
    }
}
