package com.zimdugo.push.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushReminderJobStatus;
import com.zimdugo.push.domain.PushReminderSaveCommand;
import com.zimdugo.push.domain.PushReminderStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
class PushReminderRepositoryTest {

    private static final Instant NOW = Instant.parse("2026-08-28T12:00:00Z");

    @Autowired
    private PushReminderRepository pushReminderRepository;

    @Autowired
    private PushReminderJobRepository pushReminderJobRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findsAndCountsOnlyFutureNonDeletedActiveRemindersForTheRequestedDevice() {
        PushReminderEntity activeReminder = save(7L, NOW.plusSeconds(600));
        PushReminderEntity completedReminder = save(7L, NOW.plusSeconds(900));
        completedReminder.complete();
        PushReminderEntity expiredReminder = save(7L, NOW.minusSeconds(1));
        PushReminderEntity deletedReminder = save(7L, NOW.plusSeconds(1_200));
        ReflectionTestUtils.setField(deletedReminder, "deletedAt", NOW);
        PushReminderEntity otherDeviceReminder = save(8L, NOW.plusSeconds(300));
        flushAndClear();

        assertThat(pushReminderRepository.findActiveByDeviceId(7L, NOW))
            .extracting(PushReminderEntity::getId)
            .containsExactly(activeReminder.getId());
        assertThat(pushReminderRepository.countActiveByDeviceId(7L, NOW)).isOne();
    }

    @Test
    void completesOnlyNonDeletedExpiredActiveReminders() {
        PushReminderEntity expiredReminder = save(7L, NOW.minusSeconds(1));
        PushReminderEntity deletedExpiredReminder = save(7L, NOW.minusSeconds(1));
        ReflectionTestUtils.setField(deletedExpiredReminder, "deletedAt", NOW);
        flushAndClear();

        pushReminderRepository.completeExpiredByDeviceId(
            7L, NOW, PushReminderStatus.ACTIVE, PushReminderStatus.COMPLETED
        );
        flushAndClear();

        assertThat(pushReminderRepository.findById(expiredReminder.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderStatus.COMPLETED);
        assertThat(pushReminderRepository.findById(deletedExpiredReminder.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderStatus.ACTIVE);
    }

    @Test
    void cancelsOnlyPendingJobsWhenOwnedActiveReminderIsDeleted() {
        PushReminderEntity reminder = save(7L, NOW.plusSeconds(600));
        PushReminderJobEntity startJob = saveJob(reminder.getId(), PushNotificationType.START);
        PushReminderJobEntity beforeEndJob = saveJob(reminder.getId(), PushNotificationType.BEFORE_END);
        PushReminderJobEntity endJob = saveJob(reminder.getId(), PushNotificationType.END);
        PushReminderJobEntity dispatchingJob = saveJob(reminder.getId(), PushNotificationType.START);
        PushReminderJobEntity processedJob = saveJob(reminder.getId(), PushNotificationType.END);
        processedJob.markSent(NOW.minusSeconds(1));
        flushAndClear();
        pushReminderJobRepository.claimPendingById(
            dispatchingJob.getId(),
            PushReminderJobStatus.PENDING,
            PushReminderJobStatus.DISPATCHING,
            NOW.plusSeconds(30)
        );
        flushAndClear();

        new PushReminderPersistenceAdapter(pushReminderRepository, pushReminderJobRepository)
            .cancelActiveByIdAndDeviceId(reminder.getId(), 7L, NOW);
        flushAndClear();

        assertThat(pushReminderRepository.findById(reminder.getId()).orElseThrow().getDeletedAt()).isEqualTo(NOW);
        assertThat(pushReminderJobRepository.findById(startJob.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderJobStatus.CANCELLED);
        assertThat(pushReminderJobRepository.claimPendingById(
            startJob.getId(),
            PushReminderJobStatus.PENDING,
            PushReminderJobStatus.DISPATCHING,
            NOW.plusSeconds(30)
        )).isZero();
        assertThat(pushReminderJobRepository.findById(beforeEndJob.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderJobStatus.CANCELLED);
        assertThat(pushReminderJobRepository.findById(endJob.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderJobStatus.CANCELLED);
        assertThat(pushReminderJobRepository.findById(dispatchingJob.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderJobStatus.DISPATCHING);
        assertThat(pushReminderJobRepository.findById(processedJob.getId()).orElseThrow().getProcessedAt())
            .isEqualTo(NOW.minusSeconds(1));
    }

    @Test
    void doesNotDeleteOrCancelJobsForExpiredReminder() {
        PushReminderEntity reminder = save(7L, NOW.minusSeconds(1));
        PushReminderJobEntity job = saveJob(reminder.getId(), PushNotificationType.END);
        flushAndClear();

        new PushReminderPersistenceAdapter(pushReminderRepository, pushReminderJobRepository)
            .cancelActiveByIdAndDeviceId(reminder.getId(), 7L, NOW);
        flushAndClear();

        assertThat(pushReminderRepository.findById(reminder.getId()).orElseThrow().getDeletedAt()).isNull();
        assertThat(pushReminderJobRepository.findById(job.getId()).orElseThrow().getStatus())
            .isEqualTo(PushReminderJobStatus.PENDING);
    }

    private PushReminderEntity save(Long deviceId, Instant endAt) {
        return pushReminderRepository.save(new PushReminderEntity(new PushReminderSaveCommand(
            deviceId,
            123L,
            NOW.minusSeconds(600),
            endAt,
            10,
            5
        )));
    }

    private PushReminderJobEntity saveJob(Long reminderId, PushNotificationType type) {
        return pushReminderJobRepository.save(new PushReminderJobEntity(reminderId, type, NOW.plusSeconds(30)));
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
