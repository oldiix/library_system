package com.example.library_system.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LoggingAsyncExceptionHandlerTest {

    @Test
    void handleUncaughtException_failureInVoidAsyncMethod_recordsItWithoutRethrowing() throws Exception {

        LoggingAsyncExceptionHandler handler = new LoggingAsyncExceptionHandler();
        Method method = SampleService.class.getDeclaredMethod("sendNotification", String.class);
        RuntimeException failure = new IllegalStateException("Поштовий сервер недоступний");

        assertDoesNotThrow(() -> handler.handleUncaughtException(failure, method, "Кобзар"));
        assertThat(handler.getFailureCount()).isEqualTo(1);
    }

    @SuppressWarnings("unused")
    static class SampleService {
        void sendNotification(String title) {
        }
    }
}