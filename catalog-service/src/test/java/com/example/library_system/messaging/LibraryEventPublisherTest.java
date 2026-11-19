package com.example.library_system.messaging;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.BookDeletedEvent;
import com.example.common.event.StockReportGeneratedEvent;
import com.example.library_system.entity.StockReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LibraryEventPublisherTest {

    private static final Instant NOW = Instant.parse("2026-11-19T20:00:00Z");

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private LibraryEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new LibraryEventPublisher(applicationEventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void bookCreated_publishesOneEventWithBookDataAndNewEventId() {
        publisher.bookCreated(7L, "Кобзар", "Тарас Шевченко");

        ArgumentCaptor<BookCreatedEvent> captor = ArgumentCaptor.forClass(BookCreatedEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        BookCreatedEvent event = captor.getValue();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.bookId()).isEqualTo(7L);
        assertThat(event.title()).isEqualTo("Кобзар");
        assertThat(event.authorName()).isEqualTo("Тарас Шевченко");
    }

    @Test
    void bookDeleted_publishesOneEventWithBookData() {
        publisher.bookDeleted(7L, "Кобзар");

        ArgumentCaptor<BookDeletedEvent> captor = ArgumentCaptor.forClass(BookDeletedEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        assertThat(captor.getValue().bookId()).isEqualTo(7L);
        assertThat(captor.getValue().title()).isEqualTo("Кобзар");
        assertThat(captor.getValue().occurredAt()).isEqualTo(NOW);
    }

    @Test
    void stockReportGenerated_publishesOneEventWithReportData() {
        StockReport report = new StockReport();
        report.setReportName("daily");
        report.setReportDate(LocalDate.of(2026, 11, 19));
        report.setThreshold(1);
        report.setBooksCount(2);
        report.setBookTitles("Кобзар; Лісова пісня");

        publisher.stockReportGenerated(report);

        ArgumentCaptor<StockReportGeneratedEvent> captor = ArgumentCaptor.forClass(StockReportGeneratedEvent.class);
        verify(applicationEventPublisher, times(1)).publishEvent(captor.capture());
        StockReportGeneratedEvent event = captor.getValue();
        assertThat(event.reportName()).isEqualTo("daily");
        assertThat(event.reportDate()).isEqualTo(LocalDate.of(2026, 11, 19));
        assertThat(event.threshold()).isEqualTo(1);
        assertThat(event.booksCount()).isEqualTo(2);
        assertThat(event.bookTitles()).isEqualTo("Кобзар; Лісова пісня");
    }

    @Test
    void everyPublication_getsItsOwnEventId() {
        publisher.bookDeleted(7L, "Кобзар");
        publisher.bookDeleted(7L, "Кобзар");

        ArgumentCaptor<BookDeletedEvent> captor = ArgumentCaptor.forClass(BookDeletedEvent.class);
        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues().get(0).eventId())
                .isNotEqualTo(captor.getAllValues().get(1).eventId());
    }
}
