package com.example.library_system.service;

import com.example.library_system.dto.BookDetailsResponse;
import com.example.library_system.dto.BookDto;
import com.example.library_system.external.BookRating;
import com.example.library_system.external.ExternalCatalogClient;
import com.example.library_system.external.StoreOffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookDetailsServiceTest {

    @Mock
    private BookService bookService;

    @Mock
    private ExternalCatalogClient externalCatalogClient;

    private BookDetailsService bookDetailsService;
    private BookDto kobzar;

    @BeforeEach
    void setUp() {
        bookDetailsService = new BookDetailsService(bookService, externalCatalogClient, Duration.ofMillis(200));
        kobzar = new BookDto();
        kobzar.setId(1L);
        kobzar.setTitle("Кобзар");
    }

    @Test
    void getDetails_bothServicesRespond_combinesBookRatingAndOffer() {

        when(bookService.findById(1L)).thenReturn(kobzar);
        when(externalCatalogClient.fetchRating("Кобзар"))
                .thenReturn(CompletableFuture.completedFuture(new BookRating(4.8, 120, true)));
        when(externalCatalogClient.fetchOffer("Кобзар"))
                .thenReturn(CompletableFuture.completedFuture(new StoreOffer(new BigDecimal("250"), true, true)));

        BookDetailsResponse details = bookDetailsService.getDetails(1L);


        assertThat(details.book()).isSameAs(kobzar);
        assertThat(details.rating().average()).isEqualTo(4.8);
        assertThat(details.rating().available()).isTrue();
        assertThat(details.offer().priceUah()).isEqualByComparingTo("250");
        assertThat(details.offer().available()).isTrue();
    }

    @Test
    void getDetails_ratingServiceFails_returnsFallbackRatingInsteadOfError() {

        when(bookService.findById(1L)).thenReturn(kobzar);
        when(externalCatalogClient.fetchRating("Кобзар"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Сервіс відгуків недоступний")));
        when(externalCatalogClient.fetchOffer("Кобзар"))
                .thenReturn(CompletableFuture.completedFuture(new StoreOffer(new BigDecimal("250"), true, true)));

        BookDetailsResponse details = bookDetailsService.getDetails(1L);


        assertThat(details.rating()).isEqualTo(BookRating.unavailable());
        assertThat(details.offer().available()).isTrue();
    }

    @Test
    void getDetails_bookstoreNeverResponds_stopsWaitingAfterTimeoutAndUsesFallback() {

        when(bookService.findById(1L)).thenReturn(kobzar);
        when(externalCatalogClient.fetchRating("Кобзар"))
                .thenReturn(CompletableFuture.completedFuture(new BookRating(4.8, 120, true)));
        when(externalCatalogClient.fetchOffer("Кобзар"))
                .thenReturn(new CompletableFuture<>());   // «завислий» сервіс: ніколи не завершиться

        BookDetailsResponse details = assertTimeout(Duration.ofSeconds(2),
                () -> bookDetailsService.getDetails(1L));

        assertThat(details.offer()).isEqualTo(StoreOffer.unavailable());
        assertThat(details.rating().available()).isTrue();
    }

    @Test
    void getDetails_bookNotFound_throwsNotFoundWithoutCallingExternalServices() {

        when(bookService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Книгу з id 99 не знайдено"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookDetailsService.getDetails(99L));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(externalCatalogClient);
    }
}