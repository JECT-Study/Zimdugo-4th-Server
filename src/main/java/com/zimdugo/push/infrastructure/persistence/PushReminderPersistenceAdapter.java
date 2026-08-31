package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushReminderReader;
import com.zimdugo.push.domain.PushReminderSaveCommand;
import com.zimdugo.push.domain.PushReminderSummary;
import com.zimdugo.push.domain.PushReminderStatus;
import com.zimdugo.push.domain.PushReminderStore;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushReminderPersistenceAdapter implements PushReminderReader, PushReminderStore {

    private final PushReminderRepository pushReminderRepository;
    private final PushReminderJobRepository pushReminderJobRepository;

    @Override
    public List<PushReminderSummary> findActiveByDeviceId(Long deviceId, Instant now) {
        return pushReminderRepository.findActiveByDeviceId(deviceId, now)
            .stream()
            .map(reminder -> new PushReminderSummary(
                reminder.getId(),
                reminder.getLockerId(),
                reminder.getStartedAt(),
                reminder.getEndAt(),
                reminder.getTotalUsageMinutes(),
                reminder.getRemindBeforeMinutes()
            ))
            .toList();
    }

    @Override
    public void expireActiveByDeviceId(Long deviceId, Instant now) {
        pushReminderRepository.completeExpiredByDeviceId(
            deviceId, now, PushReminderStatus.ACTIVE, PushReminderStatus.COMPLETED
        );
    }

    @Override
    public long countActiveByDeviceId(Long deviceId, Instant now) {
        return pushReminderRepository.countActiveByDeviceId(deviceId, now);
    }

    @Override
    public Long save(PushReminderSaveCommand command) {
        return pushReminderRepository.save(
            new PushReminderEntity(command)
        ).getId();
    }

    @Override
    public void schedule(Long reminderId, PushNotificationType type, Instant fireAt) {
        pushReminderJobRepository.save(new PushReminderJobEntity(reminderId, type, fireAt));
    }
}
