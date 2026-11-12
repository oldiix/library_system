package com.example.library_system.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class OverloadExceptionHandler {

    static final String RETRY_AFTER_SECONDS = "5";
    static final String MESSAGE = "Сервер зараз перевантажений, спробуйте пізніше";

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<Map<String, Object>> handleTaskRejected(TaskRejectedException exception) {
        log.warn("Пул асинхронних задач переповнений, запит відхилено: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .header(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", MESSAGE));
    }
}