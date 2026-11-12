package com.example.library_system.audit;

import java.time.Instant;

public record AuditEntry(Instant timestamp,
                         String username,
                         String action,
                         String method,
                         String arguments) {
}