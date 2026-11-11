package com.example.partner;

import com.example.common.dto.BookRating;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewsServiceTest {

    @Test
    void ratingFor_serviceAvailable_returnsStableRatingInExpectedRange() {

        ReviewsService service = new ReviewsService(Duration.ZERO, true);

        BookRating first = service.ratingFor("Кобзар");
        BookRating second = service.ratingFor("Кобзар");

        assertThat(first).isEqualTo(second);
        assertThat(first.available()).isTrue();
        assertThat(first.average()).isBetween(3.5, 4.9);
        assertThat(first.reviewsCount()).isBetween(10, 499);
    }

    @Test
    void ratingFor_serviceUnavailable_throwsServiceUnavailable() {

        ReviewsService service = new ReviewsService(Duration.ZERO, false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.ratingFor("Кобзар"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}