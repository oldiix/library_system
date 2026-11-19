package com.example.notification.messaging;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.StockReportGeneratedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LibraryEventListenerTest {

    private static final Instant NOW = Instant.parse("2026-11-20T08:00:00Z");

    @Mock
    private LibraryEventHandler handler;

    @InjectMocks
    private LibraryEventListener listener;

    @Test
    void eachEventType_isPassedToHandler() {
        BookCreatedEvent created = new BookCreatedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар", "Тарас Шевченко");
        BookDeletedEvent deleted = new BookDeletedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар");
        StockReportGeneratedEvent report = new StockReportGeneratedEvent(UUID.randomUUID(), NOW,
                "daily", LocalDate.of(2026, 11, 20), 1, 1, "Кобзар");

        listener.onBookCreated(created);
        listener.onBookDeleted(deleted);
        listener.onStockReportGenerated(report);

        verify(handler).handle(created);
        verify(handler).handle(deleted);
        verify(handler).handle(report);
    }
}
