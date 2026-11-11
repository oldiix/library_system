package com.example.library_system.notification;

import com.example.library_system.config.AsyncConfig;
import com.example.library_system.config.AsyncProperties;
import com.example.library_system.config.LoggingAsyncExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringJUnitConfig(FireAndForgetTest.TestConfig.class)
class FireAndForgetTest {

    @Autowired
    private BookNotificationService bookNotificationService;

    @Autowired
    private ControllableSender sender;

    @Autowired
    private LoggingAsyncExceptionHandler exceptionHandler;

    @BeforeEach
    void resetSender() {
        sender.reset();
    }

    @Test
    void notifyNewBook_slowSender_returnsBeforeLetterIsSentAndSendsLaterInOwnExecutor() {

        bookNotificationService.notifyNewBook("Заповіт", "Тарас Шевченко");

        assertThat(sender.sentInThread).isNull();

        sender.allowSending();

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(sender.sentInThread).startsWith("library-async-"));
    }

    @Test
    void notifyNewBook_senderFails_callerUnaffectedAndErrorRecordedByHandler() {

        sender.failNextSend();
        long failuresBefore = exceptionHandler.getFailureCount();

        bookNotificationService.notifyNewBook("Заповіт", "Тарас Шевченко");

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(exceptionHandler.getFailureCount())
                        .isEqualTo(failuresBefore + 1));
    }


    @Configuration
    @Import({AsyncConfig.class, LoggingAsyncExceptionHandler.class, BookNotificationService.class})
    static class TestConfig {

        @Bean
        AsyncProperties asyncProperties() {
            return new AsyncProperties(2, 4, 10, "library-async-");
        }

        @Bean
        ControllableSender controllableSender() {
            return new ControllableSender();
        }
    }

    static class ControllableSender implements NotificationSender {

        private CountDownLatch permission;
        private volatile boolean failNext;
        volatile String sentInThread;

        void reset() {
            permission = new CountDownLatch(1);
            failNext = false;
            sentInThread = null;
        }

        void allowSending() {
            permission.countDown();
        }

        void failNextSend() {
            failNext = true;
        }

        @Override
        public void send(String recipient, String subject, String body) {
            if (failNext) {
                throw new IllegalStateException("Поштовий сервер недоступний");
            }
            try {
                if (!permission.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Тест не дав дозволу на надсилання");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            sentInThread = Thread.currentThread().getName();
        }
    }
}