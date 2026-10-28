package com.example.library_system.external;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FakeGatewaysTest {

    @Test
    void fetchRating_serviceAvailable_returnsStableRatingInExpectedRange() {

        FakeReviewsGateway gateway = new FakeReviewsGateway(Duration.ZERO, true);


        BookRating first = gateway.fetchRating("Кобзар");
        BookRating second = gateway.fetchRating("Кобзар");


        assertThat(first).isEqualTo(second);
        assertThat(first.available()).isTrue();
        assertThat(first.average()).isBetween(3.5, 4.9);
        assertThat(first.reviewsCount()).isBetween(10, 499);
    }

    @Test
    void fetchRating_serviceUnavailable_throwsException() {

        FakeReviewsGateway gateway = new FakeReviewsGateway(Duration.ZERO, false);

        assertThrows(IllegalStateException.class, () -> gateway.fetchRating("Кобзар"));
    }

    @Test
    void fetchOffer_bookstoreAvailable_returnsStablePriceInExpectedRange() {

        FakeBookstoreGateway gateway = new FakeBookstoreGateway(Duration.ZERO, true);

        StoreOffer first = gateway.fetchOffer("Кобзар");
        StoreOffer second = gateway.fetchOffer("Кобзар");

        assertThat(first).isEqualTo(second);
        assertThat(first.available()).isTrue();
        assertThat(first.priceUah().intValue()).isBetween(150, 549);
    }

    @Test
    void fetchOffer_bookstoreUnavailable_throwsException() {

        FakeBookstoreGateway gateway = new FakeBookstoreGateway(Duration.ZERO, false);

        assertThrows(IllegalStateException.class, () -> gateway.fetchOffer("Кобзар"));
    }
}