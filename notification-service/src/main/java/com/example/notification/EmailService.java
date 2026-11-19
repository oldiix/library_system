package com.example.notification;

import com.example.common.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class EmailService {

    private final Duration sendDelay;

    public EmailService(@Value("${notification.send-delay:1s}") Duration sendDelay) {
        this.sendDelay = sendDelay;
    }

    public void send(NotificationRequest request) {
        if (request == null || isBlank(request.recipient()) || isBlank(request.subject())) {
            throw new IllegalArgumentException("Потрібні адреса отримувача і тема листа");
        }
        try {
            Thread.sleep(sendDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Надсилання листа перервано", e);
        }
        log.info("Лист надіслано на {}: «{}»", request.recipient(), request.subject());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
