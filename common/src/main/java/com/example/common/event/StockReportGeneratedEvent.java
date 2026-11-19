package com.example.common.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record StockReportGeneratedEvent(UUID eventId,
                                        Instant occurredAt,
                                        String reportName,
                                        LocalDate reportDate,
                                        int threshold,
                                        int booksCount,
                                        String bookTitles) {
}