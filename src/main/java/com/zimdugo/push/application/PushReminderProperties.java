package com.zimdugo.push.application;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "push.reminder")
public record PushReminderProperties(
    @Min(1) int minimumLeadSeconds,
    @Min(1) int maximumDurationSeconds,
    @Min(1) @Max(1) int maximumActiveCount,
    Set<Integer> allowedBeforeMinutes,
    @Min(1) int maximumCreateRequests,
    @Min(1) int createRateLimitWindowSeconds,
    @Min(1) long rateLimitCleanupFixedDelayMillis
) {
}
