package com.zimdugo.push.infrastructure.persistence;

import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushReminderSaveCommand;
import com.zimdugo.push.domain.PushReminderStatus;
import com.zimdugo.push.domain.PushReminderStore;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushReminderPersistenceAdapter implements PushReminderStore {

    private final PushReminderRepository pushReminderRepository;
    private final PushReminderJobRepository pushReminderJobRepository;

    @Override
    public void expireActiveByDeviceId(Long deviceId, Instant now) {
        pushReminderRepository.completeExpiredByDeviceId(
            deviceId, now, PushReminderStatus.ACTIVE, PushReminderStatus.COMPLETED
        );
    }

    @Override
    public long countActiveByDeviceId(Long deviceId, Instant now) {
        return pushReminderRepository.countByDeviceIdAndStatus(deviceId, PushReminderStatus.ACTIVE);
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
