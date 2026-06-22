package com.zimdugo.user.infrastructure.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class VisitorAsyncConfigTest {

    @Test
    void visitorExecutorIsBoundedAndDiscardsRejectedTelemetry() {
        ThreadPoolTaskExecutor executor = new VisitorAsyncConfig().visitorLogExecutor();
        executor.initialize();
        try {
            assertThat(executor.getCorePoolSize()).isEqualTo(1);
            assertThat(executor.getMaxPoolSize()).isEqualTo(2);
            assertThat(executor.getQueueCapacity()).isEqualTo(500);
            assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.DiscardPolicy.class);
        } finally {
            executor.shutdown();
        }
    }
}
