package com.example.library_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig implements AsyncConfigurer {

    public static final String EXECUTOR_NAME = "libraryTaskExecutor";

    private final AsyncProperties properties;
    private final LoggingAsyncExceptionHandler exceptionHandler;

    @Bean(name = EXECUTOR_NAME)
    public ThreadPoolTaskExecutor libraryTaskExecutor() {
        ThreadPoolTaskExecutor executor = newExecutor(properties);
        log.info("Пул асинхронних задач: core={}, max={}, queue={}, prefix='{}'",
                properties.corePoolSize(), properties.maxPoolSize(),
                properties.queueCapacity(), properties.threadNamePrefix());
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return libraryTaskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return exceptionHandler;
    }

    public static ThreadPoolTaskExecutor newExecutor(AsyncProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.corePoolSize());
        executor.setMaxPoolSize(properties.maxPoolSize());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setThreadNamePrefix(properties.threadNamePrefix());

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}