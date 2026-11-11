package com.example.library_system.audit;

import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class AuditTrail {

    private static final int DEFAULT_CAPACITY = 100;

    private final int capacity;
    private final Deque<AuditEntry> entries = new ConcurrentLinkedDeque<>();

    public AuditTrail() {
        this(DEFAULT_CAPACITY);
    }

    AuditTrail(int capacity) {
        this.capacity = capacity;
    }

    public void record(AuditEntry entry) {
        entries.addFirst(entry);
        while (entries.size() > capacity) {
            entries.pollLast();
        }
    }

    public List<AuditEntry> findAll() {
        return List.copyOf(entries);
    }
}