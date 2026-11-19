package com.example.notification.messaging;

import com.example.common.dto.NotificationRequest;
import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.StockReportGeneratedEvent;
import com.example.notification.EmailService;
import com.example.notification.Notification;
import com.example.notification.NotificationStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryEventHandler {

    static final String READERS = "readers@library.example";
    static final String ADMIN = "admin@library.example";

    private final EmailService emailService;
    private final NotificationStore notificationStore;

    public boolean handle(BookCreatedEvent event) {
        return process(event.eventId(), "BookCreatedEvent", new NotificationRequest(
                READERS,
                "Нова книга в бібліотеці: " + event.title(),
                "У каталозі з'явилася нова книга «" + event.title() + "», автор: " + event.authorName() + "."));
    }

    public boolean handle(BookDeletedEvent event) {
        return process(event.eventId(), "BookDeletedEvent", new NotificationRequest(
                READERS,
                "Книгу вилучено з каталогу: " + event.title(),
                "Книга «" + event.title() + "» більше недоступна в бібліотеці."));
    }

    public boolean handle(StockReportGeneratedEvent event) {
        return process(event.eventId(), "StockReportGeneratedEvent", new NotificationRequest(
                ADMIN,
                "Звіт про запаси «" + event.reportName() + "» за " + event.reportDate(),
                "Книг із запасом ≤ " + event.threshold() + ": " + event.booksCount()
                        + (event.booksCount() > 0 ? " (" + event.bookTitles() + ")" : "") + "."));
    }

    private boolean process(UUID eventId, String eventType, NotificationRequest letter) {
        if (notificationStore.isProcessed(eventId)) {
            log.info("Подію {} [{}] уже оброблено — повторну доставку пропущено", eventType, eventId);
            return false;
        }
        log.info("Отримано подію {} [{}]", eventType, eventId);
        emailService.send(letter);
        notificationStore.save(new Notification(eventId, eventType,
                letter.recipient(), letter.subject(), letter.body(), Instant.now()));
        return true;
    }
}
