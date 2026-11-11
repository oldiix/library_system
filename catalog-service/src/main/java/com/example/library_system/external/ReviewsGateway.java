package com.example.library_system.external;

import com.example.common.dto.BookRating;

public interface ReviewsGateway {

    BookRating fetchRating(String title);
}