package com.example.common.event;

import java.time.Instant;
import java.util.UUID;

public record BookDeletedEvent(UUID eventId,
                               Instant occurredAt,
                               Long bookId,
                               String title) {
}