package com.zimdugo.locker.infrastructure.adapter;

import com.zimdugo.core.exception.BusinessException;
import com.zimdugo.core.exception.ErrorCode;
import com.zimdugo.locker.domain.issue.LockerIssueReportDuplicateGuard;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockerIssueReportDuplicateGuard implements LockerIssueReportDuplicateGuard {

    private static final Duration DUPLICATE_REPORT_TTL = Duration.ofMinutes(10);
    private static final Duration VISITOR_RATE_LIMIT_TTL = Duration.ofHours(1);
    private static final Duration IP_RATE_LIMIT_TTL = Duration.ofHours(1);
    private static final int VISITOR_REPORT_LIMIT = 5;
    private static final int IP_REPORT_LIMIT = 20;
    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void checkAndReserve(Long lockerId, String reporterIdentifier, String clientIpAddress) {
        boolean hasVisitorIdentifier = hasText(reporterIdentifier);
        String ipHash = hasText(clientIpAddress) ? hash(clientIpAddress) : null;

        if (!hasVisitorIdentifier && ipHash == null) {
            return;
        }

        if (hasVisitorIdentifier) {
            reserveDuplicate(duplicateKey("visitor", lockerId, reporterIdentifier));
            enforceRateLimit(hourlyKey("visitor", reporterIdentifier), VISITOR_RATE_LIMIT_TTL, VISITOR_REPORT_LIMIT);
        }

        if (ipHash != null) {
            reserveDuplicate(duplicateKey("ip", lockerId, ipHash));
            enforceRateLimit(hourlyKey("ip", ipHash), IP_RATE_LIMIT_TTL, IP_REPORT_LIMIT);
        }
    }

    private void reserveDuplicate(String key) {
        Boolean reserved = stringRedisTemplate.opsForValue().setIfAbsent(key, "reported", DUPLICATE_REPORT_TTL);
        if (!Boolean.TRUE.equals(reserved)) {
            throw new BusinessException(ErrorCode.LOCKER_ISSUE_REPORT_DUPLICATED);
        }
    }

    private void enforceRateLimit(String key, Duration ttl, int limit) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == null) {
            throw new BusinessException(ErrorCode.LOCKER_ISSUE_REPORT_RATE_LIMITED);
        }
        if (count == 1L) {
            stringRedisTemplate.expire(key, ttl);
        }
        if (count > limit) {
            throw new BusinessException(ErrorCode.LOCKER_ISSUE_REPORT_RATE_LIMITED);
        }
    }

    private String duplicateKey(String scope, Long lockerId, String identifier) {
        return "locker-issue-report:duplicate:" + scope + ":" + lockerId + ":" + identifier;
    }

    private String hourlyKey(String scope, String identifier) {
        return "locker-issue-report:rate:" + scope + ":" + hourSlot() + ":" + identifier;
    }

    private String hourSlot() {
        return LocalDateTime.now().format(HOUR_FORMATTER);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
