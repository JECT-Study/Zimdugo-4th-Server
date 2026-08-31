package com.zimdugo.push.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushDeviceLockReader;
import com.zimdugo.push.domain.PushReminderStore;
import com.zimdugo.push.domain.PushReminderSaveCommand;
import com.zimdugo.push.domain.PushSubscriptionReader;
import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushLockerReader;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushReminderCreateServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-28T12:00:00Z");

    @Mock
    private PushDeviceReader pushDeviceReader;

    @Mock
    private PushDeviceLockReader pushDeviceLockReader;

    @Mock
    private PushSubscriptionReader pushSubscriptionReader;

    @Mock
    private PushReminderStore pushReminderStore;

    @Mock
    private PushLockerReader pushLockerReader;

    @Mock
    private PushReminderRateLimiter pushReminderRateLimiter;

    @Test
    void createsReminderWithUsageSummaryAndSchedulesStartAtStartedAt() {
        PushReminderCreateService service = service();
        PushReminderCreateCommand command = new PushReminderCreateCommand(
            "device-token-hash", 123L, NOW.plusSeconds(120), NOW.plusSeconds(730), 5
        );
        given(pushDeviceLockReader.findIdByTokenHashForUpdate(command.deviceTokenHash())).willReturn(Optional.of(7L));
        given(pushSubscriptionReader.existsByDeviceId(7L)).willReturn(true);
        given(pushLockerReader.existsById(123L)).willReturn(true);
        given(pushReminderStore.countActiveByDeviceId(7L, NOW)).willReturn(0L);
        PushReminderSaveCommand saveCommand = new PushReminderSaveCommand(
            7L, 123L, command.startedAt(), command.endAt(), 11, 5
        );
        given(pushReminderStore.save(saveCommand)).willReturn(456L);

        PushReminderResult result = service.create(command);

        assertThat(result.id()).isEqualTo(456L);
        assertThat(result.startedAt()).isEqualTo(NOW.plusSeconds(120));
        assertThat(result.totalUsageMinutes()).isEqualTo(11);
        assertThat(result.remainingMinutes()).isEqualTo(13);
        verify(pushReminderStore).expireActiveByDeviceId(7L, NOW);
        verify(pushReminderStore).save(saveCommand);
        verify(pushReminderStore).schedule(456L, PushNotificationType.START, NOW.plusSeconds(120));
        verify(pushReminderStore).schedule(456L, PushNotificationType.BEFORE_END, NOW.plusSeconds(430));
        verify(pushReminderStore).schedule(456L, PushNotificationType.END, NOW.plusSeconds(730));
    }

    @Test
    void rejectsEndTimeThatDoesNotMeetMinimumLeadTime() {
        PushReminderCreateService service = service();
        PushReminderCreateCommand command = new PushReminderCreateCommand(
            "device-token-hash", 123L, NOW, NOW.plusSeconds(59), null
        );

        assertThatThrownBy(() -> service.create(command))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.PUSH_INVALID_FIRE_TIME);
    }

    @Test
    void rejectsUsagePeriodWhenStartedAtIsNotBeforeEndAt() {
        PushReminderCreateService service = service();
        PushReminderCreateCommand command = new PushReminderCreateCommand(
            "device-token-hash", 123L, NOW.plusSeconds(600), NOW.plusSeconds(600), null
        );

        assertThatThrownBy(() -> service.create(command))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.PUSH_INVALID_FIRE_TIME);
    }

    @Test
    void rejectsStartedAtInThePast() {
        PushReminderCreateService service = service();
        PushReminderCreateCommand command = new PushReminderCreateCommand(
            "device-token-hash", 123L, NOW.minusSeconds(1), NOW.plusSeconds(120), null
        );

        assertThatThrownBy(() -> service.create(command))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.PUSH_INVALID_FIRE_TIME);
    }

    @Test
    void rejectsBeforeEndNotificationScheduledBeforeStart() {
        PushReminderCreateService service = service();
        PushReminderCreateCommand command = new PushReminderCreateCommand(
            "device-token-hash", 123L, NOW.plusSeconds(600), NOW.plusSeconds(720), 5
        );

        assertThatThrownBy(() -> service.create(command))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.PUSH_INVALID_FIRE_TIME);
    }

    private PushReminderCreateService service() {
        return new PushReminderCreateService(
            pushDeviceReader,
            pushDeviceLockReader,
            pushSubscriptionReader,
            pushReminderStore,
            pushLockerReader,
            new PushReminderProperties(60, 86_400, 1, java.util.Set.of(5, 10, 15), 10, 60, 60_000),
            pushReminderRateLimiter,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }
}
