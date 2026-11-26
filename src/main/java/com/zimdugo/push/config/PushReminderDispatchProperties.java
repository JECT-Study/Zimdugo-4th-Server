package com.zimdugo.push.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "push.reminder")
public class PushReminderDispatchProperties {

    private static final long DEFAULT_DELIVERY_TIMEOUT_MILLIS = 5_000L;

    @Min(1)
    private int deliveryTtlSeconds;

    @Min(1)
    private int deliveryRetryDelaySeconds;

    @Min(1)
    private int maximumDeliveryAttempts;

    @Min(1)
    private int dispatchClaimSeconds;

    @Min(1)
    private long deliveryTimeoutMillis = DEFAULT_DELIVERY_TIMEOUT_MILLIS;
}
