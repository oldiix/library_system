package com.example.library_system.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookNotificationService {

    static final String READERS_MAILING_LIST = "readers@library.example";

    private final NotificationSender notificationSender;

    @Async
    public void notifyNewBook(String title, String authorName) {
        log.info("Надсилаю сповіщення про нову книгу «{}»", title);
        notificationSender.send(
                READERS_MAILING_LIST,
                "Нова книга в бібліотеці: " + title,
                "У каталозі з'явилася нова книга «" + title + "», автор: " + authorName + ".");
    }
}