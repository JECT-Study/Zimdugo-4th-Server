package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushReminderJobStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface PushReminderJobRepository extends JpaRepository<PushReminderJobEntity, Long> {

    @Query("""
        select job
        from PushReminderJobEntity job
        where job.status = :status
          and job.nextAttemptAt <= :now
        order by job.fireAt asc
        """)
    List<PushReminderJobEntity> findDueJobs(
        @Param("status") PushReminderJobStatus status,
        @Param("now") Instant now,
        Pageable pageable
    );

    @Modifying
    @Query("""
        update PushReminderJobEntity job
        set job.status = :dispatchingStatus,
            job.nextAttemptAt = :leaseExpiresAt,
            job.attemptCount = job.attemptCount + 1
        where job.id = :jobId
          and job.status = :pendingStatus
        """)
    int claimPendingById(
        @Param("jobId") Long jobId,
        @Param("pendingStatus") PushReminderJobStatus pendingStatus,
        @Param("dispatchingStatus") PushReminderJobStatus dispatchingStatus,
        @Param("leaseExpiresAt") Instant leaseExpiresAt
    );

    @Modifying
    @Transactional
    @Query("""
        update PushReminderJobEntity job
        set job.status = :pendingStatus
        where job.status = :dispatchingStatus
          and job.nextAttemptAt <= :now
        """)
    void requeueExpiredDispatches(
        @Param("dispatchingStatus") PushReminderJobStatus dispatchingStatus,
        @Param("pendingStatus") PushReminderJobStatus pendingStatus,
        @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update PushReminderJobEntity job
        set job.status = :cancelledStatus,
            job.processedAt = :processedAt
        where job.reminderId = :reminderId
          and job.status = :pendingStatus
        """)
    void cancelPendingByReminderId(
        @Param("reminderId") Long reminderId,
        @Param("pendingStatus") PushReminderJobStatus pendingStatus,
        @Param("cancelledStatus") PushReminderJobStatus cancelledStatus,
        @Param("processedAt") Instant processedAt
    );
}
