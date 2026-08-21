package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.issue.LockerIssueReportDuplicateGuard;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockerIssueReportDuplicateGuard implements LockerIssueReportDuplicateGuard {

    private static final Duration DUPLICATE_REPORT_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void checkAndReserve(Long lockerId, String reporterIdentifier) {
        if (reporterIdentifier == null || reporterIdentifier.isBlank()) {
            return;
        }

        Boolean reserved = stringRedisTemplate.opsForValue().setIfAbsent(
            duplicateKey(lockerId, reporterIdentifier),
            "reported",
            DUPLICATE_REPORT_TTL
        );
        if (!Boolean.TRUE.equals(reserved)) {
            throw new BusinessException(ErrorCode.LOCKER_ISSUE_REPORT_DUPLICATED);
        }
    }

    private String duplicateKey(Long lockerId, String reporterIdentifier) {
        return "locker-issue-report:" + lockerId + ":" + reporterIdentifier;
    }
}
