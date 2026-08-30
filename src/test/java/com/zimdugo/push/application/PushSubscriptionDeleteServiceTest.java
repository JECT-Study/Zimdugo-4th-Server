package com.zimdugo.push.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushSubscriptionStore;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushSubscriptionDeleteServiceTest {

    @Mock
    private PushDeviceReader pushDeviceReader;

    @Mock
    private PushSubscriptionStore pushSubscriptionStore;

    @InjectMocks
    private PushSubscriptionDeleteService pushSubscriptionDeleteService;

    @Test
    void deletesOnlyCurrentDeviceSubscription() {
        given(pushDeviceReader.findIdByTokenHash("device-token-hash")).willReturn(Optional.of(7L));

        pushSubscriptionDeleteService.delete("device-token-hash");

        verify(pushSubscriptionStore).deleteByDeviceId(7L);
    }

    @Test
    void completesWhenDeviceCookieIsMissingOrUnknown() {
        given(pushDeviceReader.findIdByTokenHash("unknown-token-hash")).willReturn(Optional.empty());

        pushSubscriptionDeleteService.delete("unknown-token-hash");

        verify(pushSubscriptionStore, never()).deleteByDeviceId(7L);
    }
}
