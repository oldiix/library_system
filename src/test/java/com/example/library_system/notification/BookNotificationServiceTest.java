package com.example.library_system.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookNotificationServiceTest {

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private BookNotificationService bookNotificationService;

    @Test
    void notifyNewBook_newBook_sendsLetterWithTitleAndAuthorToReaders() {

        bookNotificationService.notifyNewBook("Заповіт", "Тарас Шевченко");

        ArgumentCaptor<String> recipient = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(notificationSender).send(recipient.capture(), subject.capture(), body.capture());

        assertThat(recipient.getValue()).isEqualTo(BookNotificationService.READERS_MAILING_LIST);
        assertThat(subject.getValue()).isEqualTo("Нова книга в бібліотеці: Заповіт");
        assertThat(body.getValue()).contains("«Заповіт»", "Тарас Шевченко");
    }

    @Test
    void notifyNewBook_senderFails_exceptionPropagatesToAsyncHandler() {

        doThrow(new IllegalStateException("Поштовий сервер недоступний"))
                .when(notificationSender).send(anyString(), anyString(), anyString());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookNotificationService.notifyNewBook("Заповіт", "Тарас Шевченко"));

        assertThat(exception.getMessage()).isEqualTo("Поштовий сервер недоступний");
    }
}