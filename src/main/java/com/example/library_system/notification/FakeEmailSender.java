package com.example.library_system.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class FakeEmailSender implements NotificationSender {

    private final Duration sendDelay;

    public FakeEmailSender(@Value("${library.notifications.send-delay:1s}") Duration sendDelay) {
        this.sendDelay = sendDelay;
    }

    @Override
    public void send(String recipient, String subject, String body) {
        try {
            Thread.sleep(sendDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Надсилання листа перервано", e);
        }
        log.info("Лист надіслано на {}: «{}»", recipient, subject);
    }
}