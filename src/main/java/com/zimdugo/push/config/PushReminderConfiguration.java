package com.zimdugo.push.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zimdugo.push.application.PushReminderProperties;

@Configuration
@EnableConfigurationProperties({
    PushReminderProperties.class,
    PushReminderDispatchProperties.class,
    PushVapidProperties.class
})
public class PushReminderConfiguration {

    @Bean
    public Clock pushClock() {
        return Clock.systemUTC();
    }
}
