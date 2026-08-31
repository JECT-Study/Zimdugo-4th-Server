package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.domain.PushSubscriptionReader;
import com.zimdugo.push.domain.PushSubscriptionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushSubscriptionPersistenceAdapter implements PushSubscriptionStore, PushSubscriptionReader {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Override
    public void upsert(Long deviceId, String endpoint, String p256dh, String auth, PushLocale locale) {
        pushSubscriptionRepository.findByEndpoint(endpoint)
            .ifPresentOrElse(
                subscription -> updateOrRejectDifferentDevice(subscription, deviceId, p256dh, auth, locale),
                () -> upsertNewEndpoint(deviceId, endpoint, p256dh, auth, locale)
            );
    }

    @Override
    public void deleteByDeviceId(Long deviceId) {
        pushSubscriptionRepository.deleteByDeviceId(deviceId);
    }

    @Override
    public boolean existsByDeviceId(Long deviceId) {
        return pushSubscriptionRepository.findByDeviceId(deviceId).isPresent();
    }

    private void updateOrRejectDifferentDevice(
        PushSubscriptionEntity subscription,
        Long deviceId,
        String p256dh,
        String auth,
        PushLocale locale
    ) {
        if (!subscription.getDeviceId().equals(deviceId)) {
            throw new BusinessException(ErrorCode.PUSH_SUBSCRIPTION_ENDPOINT_CONFLICT);
        }
        subscription.update(subscription.getEndpoint(), p256dh, auth, locale);
    }

    private void upsertNewEndpoint(Long deviceId, String endpoint, String p256dh, String auth, PushLocale locale) {
        // endpoint 재발급은 같은 기기의 기존 행을 갱신해 기기당 활성 구독 1개를 보장한다.
        pushSubscriptionRepository.findByDeviceId(deviceId)
            .ifPresentOrElse(
                subscription -> subscription.update(endpoint, p256dh, auth, locale),
                () -> pushSubscriptionRepository.save(
                    new PushSubscriptionEntity(deviceId, endpoint, p256dh, auth, locale)
                )
            );
    }
}
