package com.zimdugo.push.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.domain.PushSubscriptionStore;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionServiceTest {

    @Mock
    private PushDeviceReader pushDeviceReader;

    @Mock
    private PushSubscriptionStore pushSubscriptionStore;

    @InjectMocks
    private PushSubscriptionService pushSubscriptionService;

    @Test
    void registersSubscriptionForDevice() {
        PushSubscriptionCommand command = new PushSubscriptionCommand(
            "device-token-hash",
            "https://fcm.googleapis.com/fcm/send/example",
            "p256dh-key",
            "auth-key",
            "ko"
        );
        given(pushDeviceReader.findIdByTokenHash(command.deviceTokenHash())).willReturn(Optional.of(7L));

        pushSubscriptionService.upsert(command);

        verify(pushSubscriptionStore).upsert(
            7L,
            command.endpoint(),
            command.p256dh(),
            command.auth(),
            PushLocale.KO
        );
    }

    @Test
    void rejectsUnknownDevice() {
        PushSubscriptionCommand command = new PushSubscriptionCommand(
            "unknown-token-hash",
            "https://fcm.googleapis.com/fcm/send/example",
            "p256dh-key",
            "auth-key",
            "ko"
        );
        given(pushDeviceReader.findIdByTokenHash(command.deviceTokenHash())).willReturn(Optional.empty());

        assertThatThrownBy(() -> pushSubscriptionService.upsert(command))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.PUSH_DEVICE_NOT_FOUND);
    }
}
