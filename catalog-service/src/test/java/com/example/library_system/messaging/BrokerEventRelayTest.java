package com.example.library_system.messaging;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.LibraryEvents;
import com.example.common.event.StockReportGeneratedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.AmqpTemplate;

import java.net.ConnectException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BrokerEventRelayTest {

    private static final Instant NOW = Instant.parse("2026-11-19T20:00:00Z");

    @Mock
    private AmqpTemplate amqpTemplate;

    @InjectMocks
    private BrokerEventRelay relay;

    @Test
    void onBookCreated_sendsEventToExchangeWithBookCreatedKey() {
        BookCreatedEvent event = new BookCreatedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар", "Тарас Шевченко");

        relay.onBookCreated(event);

        verify(amqpTemplate).convertAndSend(LibraryEvents.EXCHANGE, LibraryEvents.BOOK_CREATED, event);
    }

    @Test
    void onBookDeleted_sendsEventToExchangeWithBookDeletedKey() {
        BookDeletedEvent event = new BookDeletedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар");

        relay.onBookDeleted(event);

        verify(amqpTemplate).convertAndSend(LibraryEvents.EXCHANGE, LibraryEvents.BOOK_DELETED, event);
    }

    @Test
    void onStockReportGenerated_sendsEventToExchangeWithReportKey() {
        StockReportGeneratedEvent event = new StockReportGeneratedEvent(UUID.randomUUID(), NOW,
                "daily", LocalDate.of(2026, 11, 19), 1, 2, "Кобзар; Лісова пісня");

        relay.onStockReportGenerated(event);

        verify(amqpTemplate).convertAndSend(LibraryEvents.EXCHANGE, LibraryEvents.STOCK_REPORT_GENERATED, event);
    }

    @Test
    void brokerUnavailable_errorIsLoggedAndDoesNotBreakCommittedAction() {
        doThrow(new AmqpConnectException(new ConnectException("Connection refused")))
                .when(amqpTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        BookCreatedEvent event = new BookCreatedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар", "Тарас Шевченко");

        assertThatCode(() -> relay.onBookCreated(event)).doesNotThrowAnyException();
    }
}
