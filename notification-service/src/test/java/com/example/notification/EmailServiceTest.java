package com.example.notification;

import com.example.common.dto.NotificationRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailServiceTest {

    private final EmailService service = new EmailService(Duration.ZERO);

    @Test
    void send_validRequest_completesWithoutError() {
        NotificationRequest letter = new NotificationRequest("readers@library.example", "Нова книга", "Текст");

        assertThatCode(() -> service.send(letter)).doesNotThrowAnyException();
    }

    @Test
    void send_blankRecipient_throwsClearError() {
        NotificationRequest letter = new NotificationRequest(" ", "Нова книга", "Текст");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.send(letter));

        assertThat(exception.getMessage()).contains("отримувача");
    }

    @Test
    void send_interrupted_throwsAndKeepsInterruptFlag() {
        EmailService slowService = new EmailService(Duration.ofSeconds(5));
        Thread.currentThread().interrupt();

        assertThrows(IllegalStateException.class, () -> slowService.send(
                new NotificationRequest("readers@library.example", "Тема", "Текст")));

        assertThat(Thread.interrupted()).isTrue();
    }
}
