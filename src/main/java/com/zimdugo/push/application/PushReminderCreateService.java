package com.zimdugo.push.application;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.push.domain.PushDeviceReader;
import com.zimdugo.push.domain.PushDeviceLockReader;
import com.zimdugo.push.domain.PushLockerReader;
import com.zimdugo.push.domain.PushNotificationType;
import com.zimdugo.push.domain.PushReminderSaveCommand;
import com.zimdugo.push.domain.PushReminderStore;
import com.zimdugo.push.domain.PushSubscriptionReader;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushReminderCreateService {

    private static final long SECONDS_PER_MINUTE = 60L;

    private final PushDeviceReader pushDeviceReader;
    private final PushDeviceLockReader pushDeviceLockReader;
    private final PushSubscriptionReader pushSubscriptionReader;
    private final PushReminderStore pushReminderStore;
    private final PushLockerReader pushLockerReader;
    private final PushReminderProperties properties;
    private final PushReminderRateLimiter pushReminderRateLimiter;
    private final Clock clock;

    public PushReminderResult create(PushReminderCreateCommand command) {
        Instant now = clock.instant();
        // 제한은 리마인더 저장 전에 검사해 한도 초과 요청이 예약 작업을 남기지 않게 한다.
        pushReminderRateLimiter.check(command.deviceTokenHash());
        validateSchedule(command, now);
        Long deviceId = pushDeviceLockReader.findIdByTokenHashForUpdate(command.deviceTokenHash())
            .orElseThrow(() -> new BusinessException(ErrorCode.PUSH_DEVICE_NOT_FOUND));
        pushReminderStore.expireActiveByDeviceId(deviceId, now);
        validatePrerequisites(command.lockerId(), deviceId, now);

        int totalUsageMinutes = minutesCeiling(command.startedAt(), command.endAt());
        Long reminderId = pushReminderStore.save(new PushReminderSaveCommand(
            deviceId,
            command.lockerId(),
            command.startedAt(),
            command.endAt(),
            totalUsageMinutes,
            command.remindBeforeMinutes()
        ));
        scheduleNotifications(reminderId, command);
        return new PushReminderResult(
            reminderId,
            command.lockerId(),
            command.startedAt(),
            command.endAt(),
            totalUsageMinutes,
            minutesCeiling(now, command.endAt()),
            command.remindBeforeMinutes()
        );
    }

    private void scheduleNotifications(Long reminderId, PushReminderCreateCommand command) {
        // 시작·종료와 선택된 사전 알림을 독립 DB 작업으로 저장해 재시작 뒤에도 남은 발송을 다시 처리할 수 있다.
        pushReminderStore.schedule(reminderId, PushNotificationType.START, command.startedAt());
        if (command.remindBeforeMinutes() != null) {
            pushReminderStore.schedule(
                reminderId,
                PushNotificationType.BEFORE_END,
                command.endAt().minusSeconds(command.remindBeforeMinutes() * SECONDS_PER_MINUTE)
            );
        }
        pushReminderStore.schedule(reminderId, PushNotificationType.END, command.endAt());
    }

    private void validateSchedule(PushReminderCreateCommand command, Instant now) {
        if (!command.startedAt().isAfter(now) ||
            !command.startedAt().isBefore(command.endAt()) ||
            command.endAt().isBefore(now.plusSeconds(properties.minimumLeadSeconds())) ||
            command.endAt().isAfter(now.plusSeconds(properties.maximumDurationSeconds())) ||
            (command.remindBeforeMinutes() != null && (
                !properties.allowedBeforeMinutes().contains(command.remindBeforeMinutes()) ||
                !command.endAt().minusSeconds(command.remindBeforeMinutes() * SECONDS_PER_MINUTE)
                    .isAfter(command.startedAt()) ||
                command.endAt().minusSeconds(command.remindBeforeMinutes() * SECONDS_PER_MINUTE)
                    .isBefore(now.plusSeconds(properties.minimumLeadSeconds()))
            ))) {
            throw new BusinessException(ErrorCode.PUSH_INVALID_FIRE_TIME);
        }
    }

    private int minutesCeiling(Instant from, Instant to) {
        Duration duration = Duration.between(from, to);
        long fullMinutes = duration.toMinutes();
        return Math.toIntExact(duration.minusMinutes(fullMinutes).isZero() ? fullMinutes : fullMinutes + 1);
    }

    private void validatePrerequisites(Long lockerId, Long deviceId, Instant now) {
        if (!pushLockerReader.existsById(lockerId)) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_FOUND);
        }
        if (!pushSubscriptionReader.existsByDeviceId(deviceId)) {
            throw new BusinessException(ErrorCode.PUSH_SUBSCRIPTION_NOT_FOUND);
        }
        if (pushReminderStore.countActiveByDeviceId(deviceId, now) >= properties.maximumActiveCount()) {
            throw new BusinessException(ErrorCode.PUSH_REMINDER_LIMIT_EXCEEDED);
        }
    }
}
