package com.zimdugo.push.application;

import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushReminderStore;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushReminderDeleteService {

    private final PushDeviceReader pushDeviceReader;
    private final PushReminderStore pushReminderStore;
    private final Clock clock;

    public void delete(String deviceTokenHash, Long reminderId) {
        // 기기 소유권이 확인된 요청만 삭제 상태를 바꿔 다른 브라우저의 리마인더를 보호한다.
        pushDeviceReader.findIdByTokenHash(deviceTokenHash)
            .ifPresent(deviceId -> pushReminderStore.cancelActiveByIdAndDeviceId(
                reminderId, deviceId, clock.instant()
            ));
    }
}
