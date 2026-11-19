package com.example.notification.messaging;

import com.example.common.dto.NotificationRequest;
import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.StockReportGeneratedEvent;
import com.example.notification.EmailService;
import com.example.notification.Notification;
import com.example.notification.NotificationStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LibraryEventHandlerTest {

    private static final Instant NOW = Instant.parse("2026-11-20T08:00:00Z");

    private EmailService emailService;
    private NotificationStore store;
    private LibraryEventHandler handler;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        store = new NotificationStore();
        handler = new LibraryEventHandler(emailService, store);
    }

    @Test
    void sameEventDeliveredTwice_letterSentOnceAndOneNotificationStored() {
        BookCreatedEvent event = new BookCreatedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар", "Тарас Шевченко");

        boolean first = handler.handle(event);
        boolean second = handler.handle(event);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        verify(emailService, times(1)).send(any(NotificationRequest.class));
        assertThat(store.findAll()).hasSize(1);
    }

    @Test
    void twoDifferentEvents_bothProcessed() {
        handler.handle(new BookCreatedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар", "Тарас Шевченко"));
        handler.handle(new BookCreatedEvent(UUID.randomUUID(), NOW, 8L, "Заповіт", "Тарас Шевченко"));

        verify(emailService, times(2)).send(any(NotificationRequest.class));
        assertThat(store.findAll()).hasSize(2);
    }

    @Test
    void bookCreated_lettersReadersAboutNewBook() {
        UUID eventId = UUID.randomUUID();

        handler.handle(new BookCreatedEvent(eventId, NOW, 7L, "Кобзар", "Тарас Шевченко"));

        Notification notification = store.findAll().get(0);
        assertThat(notification.eventId()).isEqualTo(eventId);
        assertThat(notification.eventType()).isEqualTo("BookCreatedEvent");
        assertThat(notification.recipient()).isEqualTo(LibraryEventHandler.READERS);
        assertThat(notification.subject()).contains("Нова книга", "Кобзар");
        assertThat(notification.body()).contains("Тарас Шевченко");
    }

    @Test
    void bookDeleted_lettersReadersAboutRemovedBook() {
        handler.handle(new BookDeletedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар"));

        Notification notification = store.findAll().get(0);
        assertThat(notification.eventType()).isEqualTo("BookDeletedEvent");
        assertThat(notification.recipient()).isEqualTo(LibraryEventHandler.READERS);
        assertThat(notification.subject()).contains("вилучено", "Кобзар");
    }

    @Test
    void stockReportGenerated_lettersAdminWithReportSummary() {
        handler.handle(new StockReportGeneratedEvent(UUID.randomUUID(), NOW,
                "daily", LocalDate.of(2026, 11, 20), 1, 2, "Кобзар; Лісова пісня"));

        Notification notification = store.findAll().get(0);
        assertThat(notification.eventType()).isEqualTo("StockReportGeneratedEvent");
        assertThat(notification.recipient()).isEqualTo(LibraryEventHandler.ADMIN);
        assertThat(notification.subject()).contains("daily", "2026-11-20");
        assertThat(notification.body()).contains("2", "Кобзар; Лісова пісня");
    }

    @Test
    void stockReportWithoutBooks_bodyHasNoTitleList() {
        handler.handle(new StockReportGeneratedEvent(UUID.randomUUID(), NOW,
                "weekly", LocalDate.of(2026, 11, 20), 3, 0, ""));

        assertThat(store.findAll().get(0).body()).isEqualTo("Книг із запасом ≤ 3: 0.");
    }

    @Test
    void sendingFails_eventIsNotMarkedProcessedSoRedeliveryCanRetry() {
        BookDeletedEvent event = new BookDeletedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар");
        doThrow(new IllegalStateException("Поштовий сервер недоступний"))
                .when(emailService).send(any(NotificationRequest.class));

        assertThrows(IllegalStateException.class, () -> handler.handle(event));

        assertThat(store.isProcessed(event.eventId())).isFalse();
        assertThat(store.findAll()).isEmpty();
    }
}
