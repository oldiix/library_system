package com.example.library_system.dto;

import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;

public record BookDetailsResponse(BookDto book, BookRating rating, StoreOffer offer) {
}