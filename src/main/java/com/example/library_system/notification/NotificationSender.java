package com.example.library_system.notification;

public interface NotificationSender {

    void send(String recipient, String subject, String body);
}