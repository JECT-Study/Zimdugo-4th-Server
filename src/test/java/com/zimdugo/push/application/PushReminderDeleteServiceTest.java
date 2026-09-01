package com.zimdugo.push.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushReminderStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushReminderDeleteServiceTest {

    @Mock
    private PushDeviceReader pushDeviceReader;

    @Mock
    private PushReminderStore pushReminderStore;

    @Test
    void cancelsActiveReminderAndPendingJobsOnlyWhenOwnedByCurrentDevice() {
        Instant now = Instant.parse("2026-08-31T12:00:00Z");
        PushReminderDeleteService pushReminderDeleteService = new PushReminderDeleteService(
            pushDeviceReader,
            pushReminderStore,
            Clock.fixed(now, ZoneOffset.UTC)
        );
        given(pushDeviceReader.findIdByTokenHash("device-token-hash")).willReturn(Optional.of(7L));

        pushReminderDeleteService.delete("device-token-hash", 456L);

        verify(pushReminderStore).cancelActiveByIdAndDeviceId(456L, 7L, now);
    }

    @Test
    void completesWhenDeviceCookieIsUnknown() {
        PushReminderDeleteService pushReminderDeleteService = new PushReminderDeleteService(
            pushDeviceReader,
            pushReminderStore,
            Clock.systemUTC()
        );
        given(pushDeviceReader.findIdByTokenHash("unknown-token-hash")).willReturn(Optional.empty());

        pushReminderDeleteService.delete("unknown-token-hash", 456L);

        verifyNoInteractions(pushReminderStore);
    }
}
