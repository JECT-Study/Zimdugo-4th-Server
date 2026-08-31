package com.zimdugo.push.domain;

import java.time.Instant;

public interface PushReminderStore {

    void expireActiveByDeviceId(Long deviceId, Instant now);

    long countActiveByDeviceId(Long deviceId, Instant now);

    Long save(PushReminderSaveCommand command);

    void schedule(Long reminderId, PushNotificationType type, Instant fireAt);
}
