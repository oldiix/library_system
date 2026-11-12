package com.example.library_system.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTrailTest {

    @Test
    void findAll_severalEntriesRecorded_returnsNewestFirst() {

        AuditTrail auditTrail = new AuditTrail();
        auditTrail.record(entry("BOOK_CREATED"));
        auditTrail.record(entry("BOOK_UPDATED"));

        List<AuditEntry> entries = auditTrail.findAll();

        assertThat(entries)
                .extracting(AuditEntry::action)
                .containsExactly("BOOK_UPDATED", "BOOK_CREATED");
    }

    @Test
    void record_capacityExceeded_dropsOldestEntries() {

        AuditTrail auditTrail = new AuditTrail(2);

        auditTrail.record(entry("FIRST"));
        auditTrail.record(entry("SECOND"));
        auditTrail.record(entry("THIRD"));

        assertThat(auditTrail.findAll())
                .extracting(AuditEntry::action)
                .containsExactly("THIRD", "SECOND");
    }

    private static AuditEntry entry(String action) {
        return new AuditEntry(Instant.parse("2026-10-22T10:00:00Z"), "alice", action, "method", "[]");
    }
}