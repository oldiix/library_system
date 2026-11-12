package com.example.notification;

import com.example.common.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailServiceTest {

    private final EmailService service = new EmailService(Duration.ZERO);

    @Test
    void send_validRequest_letterIsRecordedAsSent() {

        NotificationRequest letter = new NotificationRequest("readers@library.example", "Нова книга", "Текст");

        service.send(letter);

        assertThat(service.sentLetters()).containsExactly(letter);
    }

    @Test
    void send_blankRecipient_throwsBadRequestAndSendsNothing() {

        NotificationRequest letter = new NotificationRequest(" ", "Нова книга", "Текст");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.send(letter));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(service.sentLetters()).isEmpty();
    }

    @Test
    void send_interrupted_throwsAndKeepsInterruptFlag() {

        EmailService slowService = new EmailService(Duration.ofSeconds(5));
        Thread.currentThread().interrupt();

        assertThrows(IllegalStateException.class, () -> slowService.send(
                new NotificationRequest("readers@library.example", "Тема", "Текст")));

        assertThat(Thread.interrupted()).isTrue();
        assertThat(slowService.sentLetters()).isEmpty();
    }
}
