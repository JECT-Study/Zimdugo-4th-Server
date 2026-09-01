package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushReminderStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushReminderRepository extends JpaRepository<PushReminderEntity, Long> {

    @Query("""
        select reminder
        from PushReminderEntity reminder
        where reminder.deviceId = :deviceId
          and reminder.status = com.zimdugo.push.domain.PushReminderStatus.ACTIVE
          and reminder.deletedAt is null
          and reminder.endAt > :now
        order by reminder.endAt asc
        """)
    List<PushReminderEntity> findActiveByDeviceId(
        @Param("deviceId") Long deviceId,
        @Param("now") Instant now
    );

    @Query("""
        select count(reminder)
        from PushReminderEntity reminder
        where reminder.deviceId = :deviceId
          and reminder.status = com.zimdugo.push.domain.PushReminderStatus.ACTIVE
          and reminder.deletedAt is null
          and reminder.endAt > :now
        """)
    long countActiveByDeviceId(
        @Param("deviceId") Long deviceId,
        @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update PushReminderEntity reminder
        set reminder.deletedAt = :deletedAt
        where reminder.id = :reminderId
          and reminder.deviceId = :deviceId
          and reminder.status = :activeStatus
          and reminder.deletedAt is null
          and reminder.endAt > :now
        """)
    int softDeleteActiveByIdAndDeviceId(
        @Param("reminderId") Long reminderId,
        @Param("deviceId") Long deviceId,
        @Param("deletedAt") Instant deletedAt,
        @Param("activeStatus") PushReminderStatus activeStatus,
        @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update PushReminderEntity reminder
        set reminder.status = :completedStatus
        where reminder.deviceId = :deviceId
          and reminder.status = :activeStatus
          and reminder.deletedAt is null
          and reminder.endAt <= :now
        """)
    void completeExpiredByDeviceId(
        @Param("deviceId") Long deviceId,
        @Param("now") Instant now,
        @Param("activeStatus") PushReminderStatus activeStatus,
        @Param("completedStatus") PushReminderStatus completedStatus
    );
}
