package com.example.library_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    @Test
    void newExecutor_properties_createsBoundedPoolWithPrefixAndAbortPolicy() {

        AsyncProperties properties = new AsyncProperties(2, 4, 10, "library-async-");

        ThreadPoolTaskExecutor executor = AsyncConfig.newExecutor(properties);
        executor.initialize();

        try {
            assertThat(executor.getCorePoolSize()).isEqualTo(2);
            assertThat(executor.getMaxPoolSize()).isEqualTo(4);
            assertThat(executor.getQueueCapacity()).isEqualTo(10);
            assertThat(executor.getThreadNamePrefix()).isEqualTo("library-async-");
            assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                    .isInstanceOf(ThreadPoolExecutor.AbortPolicy.class);
        } finally {
            executor.shutdown();
        }
    }
}