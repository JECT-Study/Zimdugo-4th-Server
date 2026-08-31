package com.zimdugo.push.infrastructure.scheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.zimdugo.push.domain.PushLocale;
import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushSubscription;
import com.zimdugo.push.domain.WebPushSendResult;
import com.zimdugo.push.domain.WebPushSender;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobEntity;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushReminderDeliverySchedulerTest {

    @Mock
    private PushReminderJobRepository pushReminderJobRepository;

    @Mock
    private PushReminderDeliveryProcessor pushReminderDeliveryProcessor;

    @Mock
    private WebPushSender webPushSender;

    @Mock
    private PushReminderJobEntity job;

    @Test
    void preparesSendsAndCompletesDueJobInOrder() {
        Instant now = Instant.parse("2026-08-28T12:00:00Z");
        PushReminderDeliveryScheduler scheduler = new PushReminderDeliveryScheduler(
            pushReminderJobRepository,
            pushReminderDeliveryProcessor,
            webPushSender,
            Clock.fixed(now, ZoneOffset.UTC)
        );
        PushReminderDeliveryProcessor.DeliveryCandidate candidate = candidate();
        given(pushReminderJobRepository.findTop100ByProcessedAtIsNullAndNextAttemptAtLessThanEqualOrderByFireAtAsc(now))
            .willReturn(List.of(job));
        given(job.getId()).willReturn(1L);
        given(pushReminderDeliveryProcessor.prepare(1L)).willReturn(candidate);
        given(webPushSender.send(
            candidate.subscription(), candidate.type(), candidate.lockerId(), candidate.lockerName()
        ))
            .willReturn(WebPushSendResult.SENT);

        scheduler.dispatchDueJobs();

        InOrder inOrder = inOrder(pushReminderDeliveryProcessor, webPushSender);
        inOrder.verify(pushReminderDeliveryProcessor).prepare(1L);
        inOrder.verify(webPushSender).send(
            candidate.subscription(), candidate.type(), candidate.lockerId(), candidate.lockerName()
        );
        inOrder.verify(pushReminderDeliveryProcessor).complete(candidate, WebPushSendResult.SENT);
        verifyNoMoreInteractions(pushReminderDeliveryProcessor, webPushSender);
    }

    private PushReminderDeliveryProcessor.DeliveryCandidate candidate() {
        return new PushReminderDeliveryProcessor.DeliveryCandidate(
            1L,
            2L,
            PushNotificationType.START,
            2L,
            3L,
            "https://fcm.googleapis.com/fcm/send/example-endpoint",
            new PushSubscription(
                "https://fcm.googleapis.com/fcm/send/example-endpoint", "p256dh", "auth", PushLocale.KO
            ),
            "서울역 보관함"
        );
    }
}
