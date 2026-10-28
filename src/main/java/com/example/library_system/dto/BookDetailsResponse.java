package com.example.library_system.dto;

import com.example.library_system.external.BookRating;
import com.example.library_system.external.StoreOffer;

public record BookDetailsResponse(BookDto book, BookRating rating, StoreOffer offer) {
}