package com.example.library_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(AsyncExecutorThreadTest.TestConfig.class)
class AsyncExecutorThreadTest {

    @Autowired
    private ThreadNameProbe probe;

    @Test
    void asyncMethod_called_runsInOwnExecutorThreadWithConfiguredPrefix() throws Exception {

        String callerThread = Thread.currentThread().getName();

        String asyncThread = probe.currentThreadName().get(5, TimeUnit.SECONDS);

        assertThat(asyncThread)
                .startsWith("library-async-")
                .isNotEqualTo(callerThread)
                .doesNotStartWith("task-")
                .doesNotStartWith("SimpleAsyncTaskExecutor");
    }

    @Configuration
    @Import({AsyncConfig.class, LoggingAsyncExceptionHandler.class})
    static class TestConfig {

        @Bean
        AsyncProperties asyncProperties() {
            return new AsyncProperties(2, 4, 10, "library-async-");
        }

        @Bean
        ThreadNameProbe threadNameProbe() {
            return new ThreadNameProbe();
        }
    }

    static class ThreadNameProbe {

        @Async
        public CompletableFuture<String> currentThreadName() {
            return CompletableFuture.completedFuture(Thread.currentThread().getName());
        }
    }
}