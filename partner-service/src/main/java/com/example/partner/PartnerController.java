package com.example.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partner/books/{isbn}")
@RequiredArgsConstructor
public class PartnerController {

    private final ReviewsService reviewsService;
    private final BookstoreService bookstoreService;

    @GetMapping("/rating")
    public BookRating rating(@PathVariable String isbn) {
        return reviewsService.ratingFor(isbn);
    }

    @GetMapping("/offer")
    public StoreOffer offer(@PathVariable String isbn) {
        return bookstoreService.offerFor(isbn);
    }

    @GetMapping("/discount")
    public BookDiscount discount(@PathVariable String isbn) {
        return bookstoreService.discountFor(isbn);
    }
}