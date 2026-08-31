package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushReminderSaveCommand;
import com.zimdugo.push.domain.PushReminderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_reminders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "locker_id", nullable = false)
    private Long lockerId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "total_usage_minutes", nullable = false)
    private Integer totalUsageMinutes;

    @Column(name = "remind_before_minutes")
    private Integer remindBeforeMinutes;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PushReminderStatus status;

    public PushReminderEntity(PushReminderSaveCommand command) {
        this.deviceId = command.deviceId();
        this.lockerId = command.lockerId();
        this.startedAt = command.startedAt();
        this.endAt = command.endAt();
        this.totalUsageMinutes = command.totalUsageMinutes();
        this.remindBeforeMinutes = command.remindBeforeMinutes();
        this.status = PushReminderStatus.ACTIVE;
    }

    public void complete() {
        this.status = PushReminderStatus.COMPLETED;
    }
}
