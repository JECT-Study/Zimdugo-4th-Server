package com.zimdugo.push.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.push.domain.PushLocale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(PushSubscriptionPersistenceAdapter.class)
class PushSubscriptionPersistenceAdapterTest {

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Autowired
    private PushSubscriptionPersistenceAdapter pushSubscriptionPersistenceAdapter;

    @Test
    void updatesExistingSubscriptionWhenEndpointChangesForSameDevice() {
        Long deviceId = pushDeviceRepository.save(new PushDeviceEntity("device-token-hash")).getId();
        pushSubscriptionPersistenceAdapter.upsert(
            deviceId,
            "https://fcm.googleapis.com/fcm/send/old-endpoint",
            "old-p256dh",
            "old-auth",
            PushLocale.KO
        );
        Long subscriptionId = pushSubscriptionRepository.findByDeviceId(deviceId).orElseThrow().getId();

        pushSubscriptionPersistenceAdapter.upsert(
            deviceId,
            "https://fcm.googleapis.com/fcm/send/new-endpoint",
            "new-p256dh",
            "new-auth",
            PushLocale.JA
        );
        pushSubscriptionRepository.flush();

        PushSubscriptionEntity subscription = pushSubscriptionRepository.findByDeviceId(deviceId).orElseThrow();
        assertThat(subscription.getId()).isEqualTo(subscriptionId);
        assertThat(subscription.getEndpoint()).isEqualTo("https://fcm.googleapis.com/fcm/send/new-endpoint");
        assertThat(subscription.getP256dh()).isEqualTo("new-p256dh");
        assertThat(subscription.getAuth()).isEqualTo("new-auth");
        assertThat(subscription.getLocale()).isEqualTo(PushLocale.JA);
    }

    @Test
    void rejectsEndpointAlreadyOwnedByAnotherDevice() {
        Long ownerDeviceId = pushDeviceRepository.save(new PushDeviceEntity("owner-token-hash")).getId();
        Long anotherDeviceId = pushDeviceRepository.save(new PushDeviceEntity("another-token-hash")).getId();
        String endpoint = "https://fcm.googleapis.com/fcm/send/owned-endpoint";
        pushSubscriptionPersistenceAdapter.upsert(
            ownerDeviceId,
            endpoint,
            "owner-p256dh",
            "owner-auth",
            PushLocale.KO
        );

        assertThatThrownBy(() -> pushSubscriptionPersistenceAdapter.upsert(
            anotherDeviceId,
            endpoint,
            "another-p256dh",
            "another-auth",
            PushLocale.JA
        ))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.PUSH_SUBSCRIPTION_ENDPOINT_CONFLICT);
        assertThat(pushSubscriptionRepository.findByEndpoint(endpoint).orElseThrow().getDeviceId())
            .isEqualTo(ownerDeviceId);
    }
}
