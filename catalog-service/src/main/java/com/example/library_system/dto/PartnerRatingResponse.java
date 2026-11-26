package com.example.library_system.dto;

public record PartnerRatingResponse(Long bookId,
                                    String title,
                                    String isbn,
                                    double average,
                                    int reviewsCount) {
}