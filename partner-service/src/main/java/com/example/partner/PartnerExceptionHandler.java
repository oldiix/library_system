package com.example.partner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class PartnerExceptionHandler {

    @ExceptionHandler(BookNotInCatalogException.class)
    public ResponseEntity<Map<String, Object>> notFound(BookNotInCatalogException exception) {
        log.info("REST: {}", exception.getMessage());
        return body(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(PartnerTemporarilyUnavailableException.class)
    public ResponseEntity<Map<String, Object>> unavailable(PartnerTemporarilyUnavailableException exception) {
        log.warn("REST: {}", exception.getMessage());
        return body(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
    }
}