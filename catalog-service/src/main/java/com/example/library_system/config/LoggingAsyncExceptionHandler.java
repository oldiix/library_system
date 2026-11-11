package com.example.library_system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class LoggingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final AtomicLong failureCount = new AtomicLong();

    @Override
    public void handleUncaughtException(Throwable exception, Method method, Object... params) {
        long total = failureCount.incrementAndGet();
        log.error("Помилка в асинхронному методі {}.{} з аргументами {} (усього помилок: {}): {}",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                Arrays.toString(params),
                total,
                exception.getMessage(),
                exception);
    }

    public long getFailureCount() {
        return failureCount.get();
    }
}