package com.zimdugo.push.infrastructure.scheduler;

import com.zimdugo.push.config.PushReminderDispatchProperties;
import com.zimdugo.push.domain.PushLockerNameReader;
import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushReminderStatus;
import com.zimdugo.push.domain.PushSubscription;
import com.zimdugo.push.domain.WebPushSendResult;
import com.zimdugo.push.infrastructure.persistence.PushReminderEntity;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobEntity;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobRepository;
import com.zimdugo.push.infrastructure.persistence.PushReminderRepository;
import com.zimdugo.push.infrastructure.persistence.PushSubscriptionEntity;
import com.zimdugo.push.infrastructure.persistence.PushSubscriptionRepository;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PushReminderDeliveryProcessor {

    private final PushReminderJobRepository pushReminderJobRepository;
    private final PushReminderRepository pushReminderRepository;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushLockerNameReader pushLockerNameReader;
    private final PushReminderDispatchProperties properties;
    private final Clock clock;

    @Transactional
    public DeliveryCandidate prepare(Long jobId) {
        Instant now = clock.instant();
        PushReminderJobEntity job = pushReminderJobRepository.findById(jobId).orElse(null);
        if (job == null || job.getProcessedAt() != null) {
            return null;
        }
        job.recordAttempt();
        PushReminderEntity reminder = pushReminderRepository.findById(job.getReminderId()).orElse(null);
        if (reminder == null || reminder.getStatus() != PushReminderStatus.ACTIVE || reminder.getDeletedAt() != null) {
            discard(job, reminder, now);
            return null;
        }
        if (job.getFireAt().isBefore(now.minusSeconds(properties.getDeliveryTtlSeconds()))) {
            discard(job, reminder, now);
            return null;
        }
        PushSubscriptionEntity subscription = pushSubscriptionRepository.findByDeviceId(reminder.getDeviceId())
            .orElse(null);
        if (subscription == null) {
            discard(job, reminder, now);
            return null;
        }
        return candidate(job, reminder, subscription);
    }

    private DeliveryCandidate candidate(
        PushReminderJobEntity job,
        PushReminderEntity reminder,
        PushSubscriptionEntity subscription
    ) {
        return new DeliveryCandidate(
            job.getId(),
            reminder.getId(),
            job.getType(),
            reminder.getLockerId(),
            subscription.getId(),
            subscription.getEndpoint(),
            new PushSubscription(
                subscription.getEndpoint(), subscription.getP256dh(), subscription.getAuth(), subscription.getLocale()
            ),
            pushLockerNameReader.findName(reminder.getLockerId(), subscription.getLocale())
        );
    }

    @Transactional
    public void complete(DeliveryCandidate candidate, WebPushSendResult result) {
        PushReminderJobEntity job = pushReminderJobRepository.findById(candidate.jobId()).orElse(null);
        if (job == null || job.getProcessedAt() != null) {
            return;
        }
        Instant now = clock.instant();
        PushReminderEntity reminder = pushReminderRepository.findById(candidate.reminderId()).orElse(null);
        if (result == WebPushSendResult.RETRYABLE_FAILURE) {
            retryOrDiscard(job, reminder, now);
            return;
        }
        if (result == WebPushSendResult.SUBSCRIPTION_EXPIRED) {
            pushSubscriptionRepository.findById(candidate.subscriptionId())
                .filter(subscription -> subscription.getEndpoint().equals(candidate.endpoint()))
                .ifPresent(pushSubscriptionRepository::delete);
        }
        job.markProcessed(now);
        completeReminderAtEnd(job, reminder);
    }

    private void retryOrDiscard(PushReminderJobEntity job, PushReminderEntity reminder, Instant now) {
        if (job.getAttemptCount() >= properties.getMaximumDeliveryAttempts()) {
            discard(job, reminder, now);
            return;
        }
        job.retryAt(now.plusSeconds(properties.getDeliveryRetryDelaySeconds()));
    }

    private void discard(PushReminderJobEntity job, PushReminderEntity reminder, Instant now) {
        job.discard(now);
        completeReminderAtEnd(job, reminder);
    }

    private void completeReminderAtEnd(PushReminderJobEntity job, PushReminderEntity reminder) {
        if (job.getType() == PushNotificationType.END && reminder != null) {
            reminder.complete();
        }
    }

    public record DeliveryCandidate(
        Long jobId,
        Long reminderId,
        PushNotificationType type,
        Long lockerId,
        Long subscriptionId,
        String endpoint,
        PushSubscription subscription,
        String lockerName
    ) {
    }
}
