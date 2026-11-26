package com.example.library_system.controller;

import com.example.library_system.partner.PartnerDataNotFoundException;
import com.example.library_system.partner.PartnerUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class PartnerErrorHandler {

    @ExceptionHandler(PartnerDataNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(PartnerDataNotFoundException exception) {
        return body(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(PartnerUnavailableException.class)
    public ResponseEntity<Map<String, Object>> unavailable(PartnerUnavailableException exception) {
        log.warn("Партнер недоступний: {}", exception.getMessage());
        return body(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
    }
}