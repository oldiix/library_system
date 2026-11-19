package com.example.notification;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationStore {

    private final Map<UUID, Notification> notifications = new LinkedHashMap<>();

    public synchronized boolean isProcessed(UUID eventId) {
        return notifications.containsKey(eventId);
    }

    public synchronized void save(Notification notification) {
        notifications.putIfAbsent(notification.eventId(), notification);
    }

    public synchronized List<Notification> findAll() {
        return List.copyOf(new ArrayList<>(notifications.values()));
    }
}
