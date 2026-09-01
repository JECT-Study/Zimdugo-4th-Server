package com.zimdugo.push.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushReminderJobStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PushReminderJobRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-08-31T12:00:00Z");

    @Autowired
    private PushReminderJobRepository pushReminderJobRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void claimsPendingJobOnlyOnce() {
        PushReminderJobEntity job = pushReminderJobRepository.save(
            new PushReminderJobEntity(1L, PushNotificationType.START, NOW)
        );
        flushAndClear();

        assertThat(pushReminderJobRepository.claimPendingById(
            job.getId(),
            PushReminderJobStatus.PENDING,
            PushReminderJobStatus.DISPATCHING,
            NOW.plusSeconds(30)
        )).isOne();
        assertThat(pushReminderJobRepository.claimPendingById(
            job.getId(),
            PushReminderJobStatus.PENDING,
            PushReminderJobStatus.DISPATCHING,
            NOW.plusSeconds(30)
        )).isZero();
        flushAndClear();

        PushReminderJobEntity claimedJob = pushReminderJobRepository.findById(job.getId()).orElseThrow();
        assertThat(claimedJob.getStatus()).isEqualTo(PushReminderJobStatus.DISPATCHING);
        assertThat(claimedJob.getAttemptCount()).isOne();
        assertThat(claimedJob.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(30));
    }

    @Test
    void requeuesDispatchingJobAfterItsClaimLeaseExpires() {
        PushReminderJobEntity job = pushReminderJobRepository.save(
            new PushReminderJobEntity(1L, PushNotificationType.START, NOW)
        );
        flushAndClear();
        pushReminderJobRepository.claimPendingById(
            job.getId(),
            PushReminderJobStatus.PENDING,
            PushReminderJobStatus.DISPATCHING,
            NOW.minusSeconds(1)
        );
        flushAndClear();

        pushReminderJobRepository.requeueExpiredDispatches(
            PushReminderJobStatus.DISPATCHING, PushReminderJobStatus.PENDING, NOW
        );
        flushAndClear();

        assertThat(pushReminderJobRepository.findById(job.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderJobStatus.PENDING);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
