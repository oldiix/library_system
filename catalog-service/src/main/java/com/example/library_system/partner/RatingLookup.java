package com.example.library_system.partner;

import com.example.common.dto.BookRating;

public interface RatingLookup {

    BookRating findRating(String isbn);
}