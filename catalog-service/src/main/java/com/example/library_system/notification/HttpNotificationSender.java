package com.example.library_system.notification;

import com.example.common.dto.NotificationRequest;
import com.example.library_system.config.HttpClientsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class HttpNotificationSender implements NotificationSender {

    private final RestClient restClient;

    public HttpNotificationSender(@Qualifier(HttpClientsConfig.NOTIFICATION_REST_CLIENT) RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public void send(String recipient, String subject, String body) {
        restClient.post()
                .uri("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new NotificationRequest(recipient, subject, body))
                .retrieve()
                .toBodilessEntity();
        log.info("Запит на лист для {} передано в notification-service: «{}»", recipient, subject);
    }
}
