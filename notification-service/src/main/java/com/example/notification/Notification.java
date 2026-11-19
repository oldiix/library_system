package com.example.notification;

import java.time.Instant;
import java.util.UUID;

public record Notification(UUID eventId,
                           String eventType,
                           String recipient,
                           String subject,
                           String body,
                           Instant processedAt) {
}
