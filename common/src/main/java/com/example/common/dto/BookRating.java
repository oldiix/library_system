package com.example.common.dto;

/**
 * Рейтинг книги із сервісу відгуків.
 *
 * @param average      середня оцінка (null, якщо дані недоступні)
 * @param reviewsCount кількість відгуків (null, якщо дані недоступні)
 * @param available    чи вдалося отримати дані
 */
public record BookRating(Double average, Integer reviewsCount, boolean available) {
    public static BookRating unavailable() {
        return new BookRating(null, null, false);
    }
}