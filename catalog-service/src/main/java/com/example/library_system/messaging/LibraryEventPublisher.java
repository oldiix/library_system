package com.example.library_system.messaging;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.StockReportGeneratedEvent;
import com.example.library_system.entity.StockReport;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LibraryEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    public void bookCreated(Long bookId, String title, String authorName) {
        applicationEventPublisher.publishEvent(
                new BookCreatedEvent(UUID.randomUUID(), clock.instant(), bookId, title, authorName));
    }

    public void bookDeleted(Long bookId, String title) {
        applicationEventPublisher.publishEvent(
                new BookDeletedEvent(UUID.randomUUID(), clock.instant(), bookId, title));
    }

    public void stockReportGenerated(StockReport report) {
        applicationEventPublisher.publishEvent(new StockReportGeneratedEvent(
                UUID.randomUUID(),
                clock.instant(),
                report.getReportName(),
                report.getReportDate(),
                report.getThreshold(),
                report.getBooksCount(),
                report.getBookTitles()));
    }
}
