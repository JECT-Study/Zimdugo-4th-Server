package com.zimdugo.push.application;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;

import com.zimdugo.push.infrastructure.persistence.PushReminderEntity;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobEntity;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobRepository;
import com.zimdugo.push.infrastructure.persistence.PushReminderRepository;
import com.zimdugo.push.infrastructure.persistence.PushSubscriptionRepository;
import com.zimdugo.push.infrastructure.persistence.PushSubscriptionEntity;
import com.zimdugo.push.infrastructure.scheduler.PushReminderDeliveryProcessor;
import com.zimdugo.push.config.PushReminderDispatchProperties;
import com.zimdugo.push.domain.WebPushSender;
import com.zimdugo.push.domain.PushLockerNameReader;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushReminderDeliveryServiceTest {

    @Mock
    private PushReminderJobRepository pushReminderJobRepository;

    @Mock
    private PushReminderRepository pushReminderRepository;

    @Mock
    private PushSubscriptionRepository pushSubscriptionRepository;

    @Mock
    private WebPushSender webPushSender;

    @Mock
    private PushLockerNameReader pushLockerNameReader;

    @Mock
    private PushReminderJobEntity job;

    @Mock
    private PushReminderEntity reminder;

    @Mock
    private PushSubscriptionEntity subscription;

    @Test
    void marksJobProcessedWhenReminderWasDeleted() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        PushReminderDeliveryProcessor service = new PushReminderDeliveryProcessor(
            pushReminderJobRepository,
            pushReminderRepository,
            pushSubscriptionRepository,
            pushLockerNameReader,
            properties(),
            Clock.fixed(now, ZoneOffset.UTC)
        );
        given(pushReminderJobRepository.findById(1L)).willReturn(Optional.of(job));
        given(job.getReminderId()).willReturn(2L);
        given(pushReminderRepository.findById(2L)).willReturn(Optional.of(reminder));
        given(reminder.getStatus()).willReturn(com.zimdugo.push.domain.PushReminderStatus.ACTIVE);
        given(reminder.getDeletedAt()).willReturn(now.minusSeconds(1));

        service.prepare(1L);

        verify(job).recordAttempt();
        verify(job).discard(now);
        verifyNoInteractions(pushSubscriptionRepository, webPushSender);
    }

    @Test
    void marksOverdueJobProcessedWithoutLoadingReminder() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        PushReminderDeliveryProcessor service = new PushReminderDeliveryProcessor(
            pushReminderJobRepository,
            pushReminderRepository,
            pushSubscriptionRepository,
            pushLockerNameReader,
            properties(),
            Clock.fixed(now, ZoneOffset.UTC)
        );
        given(pushReminderJobRepository.findById(1L)).willReturn(Optional.of(job));
        given(job.getFireAt()).willReturn(now.minusSeconds(61));
        given(job.getReminderId()).willReturn(2L);
        given(pushReminderRepository.findById(2L)).willReturn(Optional.of(reminder));
        given(reminder.getStatus()).willReturn(com.zimdugo.push.domain.PushReminderStatus.ACTIVE);

        service.prepare(1L);

        verify(job).recordAttempt();
        verify(job).discard(now);
        verifyNoInteractions(pushSubscriptionRepository, webPushSender);
    }

    @Test
    void preservesRenewedSubscriptionWhenExpiredResponseBelongsToOldEndpoint() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        PushReminderDeliveryProcessor service = new PushReminderDeliveryProcessor(
            pushReminderJobRepository,
            pushReminderRepository,
            pushSubscriptionRepository,
            pushLockerNameReader,
            properties(),
            Clock.fixed(now, ZoneOffset.UTC)
        );
        PushReminderDeliveryProcessor.DeliveryCandidate candidate = new PushReminderDeliveryProcessor.DeliveryCandidate(
            1L,
            2L,
            com.zimdugo.push.domain.PushNotificationType.END,
            2L,
            3L,
            "https://fcm.googleapis.com/fcm/send/old-endpoint",
            new com.zimdugo.push.domain.PushSubscription(
                "https://fcm.googleapis.com/fcm/send/old-endpoint", "p256dh", "auth", com.zimdugo.push.domain.PushLocale.KO
            ),
            "서울역 보관함"
        );
        given(pushReminderJobRepository.findById(1L)).willReturn(Optional.of(job));
        given(job.getProcessedAt()).willReturn(null);
        given(pushReminderRepository.findById(2L)).willReturn(Optional.of(reminder));
        given(pushSubscriptionRepository.findById(3L)).willReturn(Optional.of(subscription));
        given(subscription.getEndpoint()).willReturn("https://fcm.googleapis.com/fcm/send/new-endpoint");

        service.complete(candidate, com.zimdugo.push.domain.WebPushSendResult.SUBSCRIPTION_EXPIRED);

        verify(pushSubscriptionRepository, never()).delete(subscription);
        verify(job).markProcessed(now);
    }

    @Test
    void schedulesRetryAfterRetryableFailureBeforeAttemptLimit() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        PushReminderDeliveryProcessor service = new PushReminderDeliveryProcessor(
            pushReminderJobRepository,
            pushReminderRepository,
            pushSubscriptionRepository,
            pushLockerNameReader,
            properties(),
            Clock.fixed(now, ZoneOffset.UTC)
        );
        PushReminderDeliveryProcessor.DeliveryCandidate candidate = candidate();
        given(pushReminderJobRepository.findById(1L)).willReturn(Optional.of(job));
        given(job.getProcessedAt()).willReturn(null);
        given(job.getAttemptCount()).willReturn(1);
        given(pushReminderRepository.findById(2L)).willReturn(Optional.of(reminder));

        service.complete(candidate, com.zimdugo.push.domain.WebPushSendResult.RETRYABLE_FAILURE);

        verify(job).retryAt(now.plusSeconds(5));
    }

    private PushReminderDeliveryProcessor.DeliveryCandidate candidate() {
        return new PushReminderDeliveryProcessor.DeliveryCandidate(
            1L,
            2L,
            com.zimdugo.push.domain.PushNotificationType.END,
            2L,
            3L,
            "https://fcm.googleapis.com/fcm/send/endpoint",
            new com.zimdugo.push.domain.PushSubscription(
                "https://fcm.googleapis.com/fcm/send/endpoint", "p256dh", "auth", com.zimdugo.push.domain.PushLocale.KO
            ),
            "서울역 보관함"
        );
    }

    private PushReminderDispatchProperties properties() {
        PushReminderDispatchProperties properties = new PushReminderDispatchProperties();
        properties.setDeliveryTtlSeconds(60);
        properties.setDeliveryRetryDelaySeconds(5);
        properties.setMaximumDeliveryAttempts(3);
        return properties;
    }
}
