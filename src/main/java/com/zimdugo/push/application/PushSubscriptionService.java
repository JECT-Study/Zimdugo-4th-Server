package com.zimdugo.push.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.domain.PushSubscriptionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushSubscriptionService {

    private final PushDeviceReader pushDeviceReader;
    private final PushSubscriptionStore pushSubscriptionStore;

    public void upsert(PushSubscriptionCommand command) {
        Long deviceId = pushDeviceReader.findIdByTokenHash(command.deviceTokenHash())
            .orElseThrow(() -> new BusinessException(ErrorCode.PUSH_DEVICE_NOT_FOUND));
        pushSubscriptionStore.upsert(
            deviceId,
            command.endpoint(),
            command.p256dh(),
            command.auth(),
            PushLocale.from(command.locale())
        );
    }
}
