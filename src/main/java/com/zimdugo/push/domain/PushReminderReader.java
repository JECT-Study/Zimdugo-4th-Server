package com.zimdugo.push.domain;

import java.time.Instant;
import java.util.List;

public interface PushReminderReader {

    List<PushReminderSummary> findActiveByDeviceId(Long deviceId, Instant now);
}
