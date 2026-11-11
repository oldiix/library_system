package com.example.notification;

import com.example.common.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class EmailService {

    private final Duration sendDelay;
    private final List<NotificationRequest> sent = new CopyOnWriteArrayList<>();

    public EmailService(@Value("${notification.send-delay:1s}") Duration sendDelay) {
        this.sendDelay = sendDelay;
    }

    public void send(NotificationRequest request) {
        if (request == null || isBlank(request.recipient()) || isBlank(request.subject())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Потрібні адреса отримувача і тема листа");
        }
        try {
            Thread.sleep(sendDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Надсилання листа перервано", e);
        }
        sent.add(request);
        log.info("Лист надіслано на {}: «{}»", request.recipient(), request.subject());
    }

    public List<NotificationRequest> sentLetters() {
        return List.copyOf(sent);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
