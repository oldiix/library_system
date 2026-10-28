package com.example.library_system.notification;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FakeEmailSenderTest {

    @Test
    void send_zeroDelay_completesWithoutErrors() {

        FakeEmailSender sender = new FakeEmailSender(Duration.ZERO);

        assertDoesNotThrow(() -> sender.send("readers@library.example", "Тема", "Текст"));
    }
}