package com.example.notification.messaging;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.StockReportGeneratedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@RabbitListener(queues = MessagingConfig.QUEUE)
public class LibraryEventListener {

    private final LibraryEventHandler handler;

    @RabbitHandler
    public void onBookCreated(BookCreatedEvent event) {
        handler.handle(event);
    }

    @RabbitHandler
    public void onBookDeleted(BookDeletedEvent event) {
        handler.handle(event);
    }

    @RabbitHandler
    public void onStockReportGenerated(StockReportGeneratedEvent event) {
        handler.handle(event);
    }
}
