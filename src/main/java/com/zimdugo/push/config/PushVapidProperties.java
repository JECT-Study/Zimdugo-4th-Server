package com.zimdugo.push.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "push.vapid")
public class PushVapidProperties {

    @NotBlank
    private String publicKey;

    private String privateKey;

    private String subject;
}
