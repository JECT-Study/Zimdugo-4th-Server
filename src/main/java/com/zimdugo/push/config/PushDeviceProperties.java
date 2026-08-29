package com.zimdugo.push.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "push.device")
public class PushDeviceProperties {

    private boolean cookieSecure;
}
