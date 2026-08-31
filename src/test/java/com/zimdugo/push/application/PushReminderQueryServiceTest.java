package com.zimdugo.push.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.BDDMockito.given;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushReminderReader;
import com.zimdugo.push.domain.PushReminderSummary;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushReminderQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-28T12:00:00Z");

    @Mock
    private PushDeviceReader pushDeviceReader;

    @Mock
    private PushReminderReader pushReminderReader;

    @Test
    void returnsStoredUsageAndRemainingMinutesForTheCurrentDevice() {
        PushReminderQueryService service = new PushReminderQueryService(
            pushDeviceReader, pushReminderReader, Clock.fixed(NOW, ZoneOffset.UTC)
        );
        Instant startedAt = NOW.minusSeconds(299);
        Instant endAt = NOW.plusSeconds(600);
        given(pushDeviceReader.findIdByTokenHash("device-token-hash")).willReturn(Optional.of(7L));
        given(pushReminderReader.findActiveByDeviceId(7L, NOW)).willReturn(List.of(
            new PushReminderSummary(456L, 123L, startedAt, endAt, 42, 5)
        ));

        assertThat(service.getActive("device-token-hash"))
            .containsExactly(new PushReminderResult(456L, 123L, startedAt, endAt, 42, 10, 5));
    }

    @Test
    void returnsAnEmptyListWhenTheDeviceDoesNotExist() {
        PushReminderQueryService service = new PushReminderQueryService(
            pushDeviceReader, pushReminderReader, Clock.fixed(NOW, ZoneOffset.UTC)
        );
        given(pushDeviceReader.findIdByTokenHash("unknown-device-token-hash")).willReturn(Optional.empty());

        assertThat(service.getActive("unknown-device-token-hash")).isEmpty();
    }

    @Test
    void roundsRemainingMinutesUpFromTheServerTimeSnapshot() {
        Instant startedAt = NOW.minusSeconds(59);
        Instant endAt = NOW.plusSeconds(601);

        PushReminderResult result = PushReminderResult.from(
            new PushReminderSummary(456L, 123L, startedAt, endAt, 11, 5), NOW
        );

        assertThat(result.remainingMinutes()).isEqualTo(11);
    }

    @Test
    void rejectsReminderWhoseStartIsNotBeforeItsEnd() {
        Instant sameTime = Instant.parse("2026-08-28T12:00:00Z");

        assertThatIllegalArgumentException().isThrownBy(
            () -> new PushReminderSummary(456L, 123L, sameTime, sameTime, 10, 5)
        );
    }
}
