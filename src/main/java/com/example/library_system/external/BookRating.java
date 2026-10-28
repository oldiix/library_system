package com.example.library_system.external;

public record BookRating(Double average, Integer reviewsCount, boolean available) {

    public static BookRating unavailable() {
        return new BookRating(null, null, false);
    }
}