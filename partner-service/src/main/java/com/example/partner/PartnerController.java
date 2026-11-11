package com.example.partner;

import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/partner")
@RequiredArgsConstructor
public class PartnerController {

    private final ReviewsService reviewsService;
    private final BookstoreService bookstoreService;

    @GetMapping("/ratings")
    public BookRating rating(@RequestParam String title) {
        return reviewsService.ratingFor(title);
    }

    @GetMapping("/offers")
    public StoreOffer offer(@RequestParam String title) {
        return bookstoreService.offerFor(title);
    }
}