package com.example.library_system.service;

import com.example.library_system.dto.BookDetailsResponse;
import com.example.library_system.dto.BookDto;
import com.example.library_system.external.BookRating;
import com.example.library_system.external.ExternalCatalogClient;
import com.example.library_system.external.StoreOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class BookDetailsService {

    private final BookService bookService;
    private final ExternalCatalogClient externalCatalogClient;
    private final Duration externalCallTimeout;

    public BookDetailsService(BookService bookService,
                              ExternalCatalogClient externalCatalogClient,
                              @Value("${library.external.timeout:2s}") Duration externalCallTimeout) {
        this.bookService = bookService;
        this.externalCatalogClient = externalCatalogClient;
        this.externalCallTimeout = externalCallTimeout;
    }

    public BookDetailsResponse getDetails(Long bookId) {
        BookDto book = bookService.findById(bookId);

        // Обидва виклики запускаються одразу, жоден не чекає на інший
        CompletableFuture<BookRating> rating = withTimeoutAndFallback(
                externalCatalogClient.fetchRating(book.getTitle()),
                BookRating.unavailable(), "Сервіс відгуків");
        CompletableFuture<StoreOffer> offer = withTimeoutAndFallback(
                externalCatalogClient.fetchOffer(book.getTitle()),
                StoreOffer.unavailable(), "Книгарня-партнер");

        return rating
                .thenCombine(offer, (bookRating, storeOffer) -> new BookDetailsResponse(book, bookRating, storeOffer))
                .join();
    }

    private <T> CompletableFuture<T> withTimeoutAndFallback(CompletableFuture<T> call, T fallback, String source) {
        return call
                .orTimeout(externalCallTimeout.toMillis(), TimeUnit.MILLISECONDS)
                .handle((result, error) -> {
                    if (error == null) {
                        return result;
                    }
                    Throwable cause = error instanceof CompletionException && error.getCause() != null
                            ? error.getCause()
                            : error;
                    if (cause instanceof TimeoutException) {
                        log.warn("{} не відповів за {} мс — використовую запасне значення",
                                source, externalCallTimeout.toMillis());
                    } else {
                        log.warn("{} повернув помилку «{}» — використовую запасне значення",
                                source, cause.getMessage());
                    }
                    return fallback;
                });
    }
}