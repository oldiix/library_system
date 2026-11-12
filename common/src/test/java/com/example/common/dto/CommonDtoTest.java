package com.example.common.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CommonDtoTest {

    @Test
    void bookRatingUnavailable_hasNoDataAndIsMarkedUnavailable() {

        BookRating rating = BookRating.unavailable();

        assertThat(rating.available()).isFalse();
        assertThat(rating.average()).isNull();
        assertThat(rating.reviewsCount()).isNull();
    }

    @Test
    void storeOfferUnavailable_hasNoDataAndIsMarkedUnavailable() {

        StoreOffer offer = StoreOffer.unavailable();

        assertThat(offer.available()).isFalse();
        assertThat(offer.priceUah()).isNull();
        assertThat(offer.inStock()).isNull();
    }

    @Test
    void records_withSameData_areEqual() {

        BookRating first = new BookRating(4.5, 120, true);
        BookRating second = new BookRating(4.5, 120, true);
        NotificationRequest letter = new NotificationRequest("readers@library.example", "Тема", "Текст");

        assertThat(first).isEqualTo(second);
        assertThat(new StoreOffer(new BigDecimal("250"), true, true).priceUah()).isEqualByComparingTo("250");
        assertThat(letter.recipient()).isEqualTo("readers@library.example");
    }
}