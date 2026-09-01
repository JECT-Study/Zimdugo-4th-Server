package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushReminderJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_reminder_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushReminderJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reminder_id", nullable = false)
    private Long reminderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PushNotificationType type;

    @Column(name = "fire_at", nullable = false)
    private Instant fireAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PushReminderJobStatus status;

    public PushReminderJobEntity(Long reminderId, PushNotificationType type, Instant fireAt) {
        this.reminderId = reminderId;
        this.type = type;
        this.fireAt = fireAt;
        this.nextAttemptAt = fireAt;
        this.status = PushReminderJobStatus.PENDING;
    }

    public void markSent(Instant processedAt) {
        this.status = PushReminderJobStatus.SENT;
        this.processedAt = processedAt;
    }

    public void discard(Instant processedAt) {
        this.status = PushReminderJobStatus.DISCARDED;
        this.processedAt = processedAt;
    }

    public void retryAt(Instant nextAttemptAt) {
        this.status = PushReminderJobStatus.PENDING;
        this.nextAttemptAt = nextAttemptAt;
    }
}
