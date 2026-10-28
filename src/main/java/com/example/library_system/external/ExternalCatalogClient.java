package com.example.library_system.external;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ExternalCatalogClient {

    private final ReviewsGateway reviewsGateway;
    private final BookstoreGateway bookstoreGateway;

    @Async
    public CompletableFuture<BookRating> fetchRating(String title) {
        return CompletableFuture.completedFuture(reviewsGateway.fetchRating(title));
    }

    @Async
    public CompletableFuture<StoreOffer> fetchOffer(String title) {
        return CompletableFuture.completedFuture(bookstoreGateway.fetchOffer(title));
    }
}