package com.example.common.dto;

/**
 * Запит на надсилання листа: catalog-service -> notification-service.
 *
 * @param recipient адреса отримувача
 * @param subject   тема листа
 * @param body      текст листа
 */
public record NotificationRequest(String recipient, String subject, String body) {
}