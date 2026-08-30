package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.domain.PushSubscriptionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushSubscriptionPersistenceAdapter implements PushSubscriptionStore {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Override
    public void upsert(Long deviceId, String endpoint, String p256dh, String auth, PushLocale locale) {
        // 기기당 활성 구독은 하나다. endpoint가 바뀌어도 같은 행을 갱신한다.
        pushSubscriptionRepository.findByDeviceId(deviceId)
            .ifPresentOrElse(
                subscription -> subscription.update(endpoint, p256dh, auth, locale),
                () -> pushSubscriptionRepository.findByEndpoint(endpoint)
                    .ifPresentOrElse(
                        subscription -> rejectDifferentDevice(deviceId, subscription),
                        () -> pushSubscriptionRepository.save(
                            new PushSubscriptionEntity(deviceId, endpoint, p256dh, auth, locale)
                        )
                    )
            );
    }

    @Override
    public void deleteByDeviceId(Long deviceId) {
        pushSubscriptionRepository.deleteByDeviceId(deviceId);
    }

    private void rejectDifferentDevice(Long deviceId, PushSubscriptionEntity subscription) {
        if (!subscription.getDeviceId().equals(deviceId)) {
            throw new BusinessException(ErrorCode.PUSH_SUBSCRIPTION_ENDPOINT_CONFLICT);
        }
    }
}
