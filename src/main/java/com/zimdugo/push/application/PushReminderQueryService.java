package com.zimdugo.push.application;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushReminderReader;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PushReminderQueryService {

    private final PushDeviceReader pushDeviceReader;
    private final PushReminderReader pushReminderReader;
    private final Clock clock;

    public List<PushReminderResult> getActive(String deviceTokenHash) {
        return pushDeviceReader.findIdByTokenHash(deviceTokenHash)
            .map(this::getActiveByDeviceId)
            .orElseGet(List::of);
    }

    private List<PushReminderResult> getActiveByDeviceId(Long deviceId) {
        Instant now = clock.instant();
        return pushReminderReader.findActiveByDeviceId(deviceId, now)
            .stream()
            .map(summary -> PushReminderResult.from(summary, now))
            .toList();
    }
}
