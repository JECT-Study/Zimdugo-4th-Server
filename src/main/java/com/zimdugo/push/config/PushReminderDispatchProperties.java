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

    @Min(1)
    private int deliveryTtlSeconds;

    @Min(1)
    private int deliveryRetryDelaySeconds;

    @Min(1)
    private int maximumDeliveryAttempts;

    @Min(1)
    private int dispatchClaimSeconds;
}
