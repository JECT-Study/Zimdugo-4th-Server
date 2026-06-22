package com.zimdugo.user.infrastructure.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class VisitorAsyncConfig {

    private static final int QUEUE_CAPACITY = 500;

    @Bean(name = "visitorLogExecutor")
    public ThreadPoolTaskExecutor visitorLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("visitor-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        return executor;
    }
}
