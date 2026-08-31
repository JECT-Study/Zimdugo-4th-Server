package com.zimdugo.push.infrastructure.scheduler;

import com.zimdugo.push.domain.WebPushSendResult;
import com.zimdugo.push.domain.WebPushSender;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobEntity;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobRepository;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushReminderDeliveryScheduler {

    private final PushReminderJobRepository pushReminderJobRepository;
    private final PushReminderDeliveryProcessor pushReminderDeliveryProcessor;
    private final WebPushSender webPushSender;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${push.reminder.dispatch-fixed-delay-millis:1000}")
    public void dispatchDueJobs() {
        // DB가 미처리 작업의 기준이므로 재시작 뒤에도 다음 폴링에서 발송 후보를 복구한다.
        List<PushReminderJobEntity> jobs = pushReminderJobRepository
            .findTop100ByProcessedAtIsNullAndNextAttemptAtLessThanEqualOrderByFireAtAsc(clock.instant());
        jobs.forEach(job -> dispatch(job.getId()));
    }

    public void dispatch(Long jobId) {
        PushReminderDeliveryProcessor.DeliveryCandidate candidate = pushReminderDeliveryProcessor.prepare(jobId);
        if (candidate == null) {
            return;
        }
        WebPushSendResult result = webPushSender.send(
            candidate.subscription(), candidate.type(), candidate.lockerId(), candidate.lockerName()
        );
        pushReminderDeliveryProcessor.complete(candidate, result);
    }
}
