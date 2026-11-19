package com.example.common.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryEventsTest {

    private static final Instant NOW = Instant.parse("2026-11-19T20:00:00Z");

    @Test
    void contract_exchangeNamedAndRoutingKeysDistinct() {
        assertThat(LibraryEvents.EXCHANGE).isEqualTo("library.events");
        assertThat(List.of(
                LibraryEvents.BOOK_CREATED,
                LibraryEvents.BOOK_DELETED,
                LibraryEvents.STOCK_REPORT_GENERATED))
                .doesNotHaveDuplicates()
                .allMatch(key -> !key.isBlank());
    }

    @Test
    void bookCreatedEvent_keepsAllData() {
        UUID eventId = UUID.randomUUID();

        BookCreatedEvent event = new BookCreatedEvent(eventId, NOW, 7L, "Кобзар", "Тарас Шевченко");

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.bookId()).isEqualTo(7L);
        assertThat(event.title()).isEqualTo("Кобзар");
        assertThat(event.authorName()).isEqualTo("Тарас Шевченко");
    }

    @Test
    void bookDeletedEvent_keepsAllData() {
        UUID eventId = UUID.randomUUID();

        BookDeletedEvent event = new BookDeletedEvent(eventId, NOW, 7L, "Кобзар");

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.bookId()).isEqualTo(7L);
        assertThat(event.title()).isEqualTo("Кобзар");
    }

    @Test
    void stockReportGeneratedEvent_keepsAllData() {
        UUID eventId = UUID.randomUUID();

        StockReportGeneratedEvent event = new StockReportGeneratedEvent(eventId, NOW,
                "daily", LocalDate.of(2026, 11, 19), 1, 2, "Кобзар; Лісова пісня");

        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.occurredAt()).isEqualTo(NOW);
        assertThat(event.reportName()).isEqualTo("daily");
        assertThat(event.reportDate()).isEqualTo(LocalDate.of(2026, 11, 19));
        assertThat(event.threshold()).isEqualTo(1);
        assertThat(event.booksCount()).isEqualTo(2);
        assertThat(event.bookTitles()).isEqualTo("Кобзар; Лісова пісня");
    }

    @Test
    void events_sameEventId_areEqual_differentEventId_areNot() {
        UUID eventId = UUID.randomUUID();

        BookDeletedEvent original = new BookDeletedEvent(eventId, NOW, 7L, "Кобзар");
        BookDeletedEvent redelivered = new BookDeletedEvent(eventId, NOW, 7L, "Кобзар");
        BookDeletedEvent another = new BookDeletedEvent(UUID.randomUUID(), NOW, 7L, "Кобзар");

        assertThat(redelivered).isEqualTo(original).hasSameHashCodeAs(original);
        assertThat(another).isNotEqualTo(original);
    }
}