package com.example.library_system.messaging;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.LibraryEvents;
import com.example.common.event.StockReportGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrokerEventRelay {

    private final AmqpTemplate amqpTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookCreated(BookCreatedEvent event) {
        send(LibraryEvents.BOOK_CREATED, event, event.eventId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookDeleted(BookDeletedEvent event) {
        send(LibraryEvents.BOOK_DELETED, event, event.eventId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockReportGenerated(StockReportGeneratedEvent event) {
        send(LibraryEvents.STOCK_REPORT_GENERATED, event, event.eventId());
    }

    private void send(String routingKey, Object event, UUID eventId) {
        try {
            amqpTemplate.convertAndSend(LibraryEvents.EXCHANGE, routingKey, event);
            log.info("Подію {} [{}] опубліковано в {} з ключем {}",
                    event.getClass().getSimpleName(), eventId, LibraryEvents.EXCHANGE, routingKey);
        } catch (AmqpException exception) {
            log.error("Не вдалося опублікувати подію {} [{}]: {}",
                    event.getClass().getSimpleName(), eventId, exception.getMessage());
        }
    }
}
