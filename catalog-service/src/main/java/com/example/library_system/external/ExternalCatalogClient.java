package com.example.library_system.external;

import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.library_system.partner.OfferLookup;
import com.example.library_system.partner.RatingLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ExternalCatalogClient {

    private final RatingLookup ratingLookup;
    private final OfferLookup offerLookup;

    @Async
    public CompletableFuture<BookRating> fetchRating(String isbn) {
        return CompletableFuture.completedFuture(ratingLookup.findRating(isbn));
    }

    @Async
    public CompletableFuture<StoreOffer> fetchOffer(String isbn) {
        return CompletableFuture.completedFuture(offerLookup.findOffer(isbn));
    }
}