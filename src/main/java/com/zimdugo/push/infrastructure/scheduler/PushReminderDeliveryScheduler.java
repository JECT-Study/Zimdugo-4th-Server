package com.zimdugo.push.infrastructure.scheduler;

import com.zimdugo.push.domain.PushReminderJobStatus;
import com.zimdugo.push.domain.WebPushSendResult;
import com.zimdugo.push.domain.WebPushSender;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobEntity;
import com.zimdugo.push.infrastructure.persistence.PushReminderJobRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushReminderDeliveryScheduler {

    private static final int DUE_JOB_BATCH_SIZE = 100;

    private final PushReminderJobRepository pushReminderJobRepository;
    private final PushReminderDeliveryProcessor pushReminderDeliveryProcessor;
    private final WebPushSender webPushSender;
    private final Clock clock;

    @Scheduled(fixedDelayString = "${push.reminder.dispatch-fixed-delay-millis:1000}")
    public void dispatchDueJobs() {
        // 리스가 만료된 선점은 재시도 대기로 되돌려 서버 중단 뒤에도 발송 후보를 복구한다.
        Instant now = clock.instant();
        pushReminderJobRepository.requeueExpiredDispatches(
            PushReminderJobStatus.DISPATCHING, PushReminderJobStatus.PENDING, now
        );
        List<PushReminderJobEntity> jobs = pushReminderJobRepository
            .findDueJobs(PushReminderJobStatus.PENDING, now, PageRequest.of(0, DUE_JOB_BATCH_SIZE));
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
