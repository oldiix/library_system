package com.example.partner;

import com.example.common.dto.BookRating;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewsServiceTest {

    private final PartnerCatalog catalog = new PartnerCatalog();

    @Test
    void ratingFor_knownIsbn_returnsRatingFromCatalog() {
        ReviewsService service = new ReviewsService(catalog, Duration.ZERO, true);

        BookRating rating = service.ratingFor("978-966-01-0001-1");

        assertThat(rating).isEqualTo(new BookRating(4.8, 312, true));
    }

    @Test
    void ratingFor_unknownIsbn_throwsNotInCatalog() {
        ReviewsService service = new ReviewsService(catalog, Duration.ZERO, true);

        assertThrows(BookNotInCatalogException.class, () -> service.ratingFor("978-0-00-000000-0"));
    }

    @Test
    void ratingFor_serviceUnavailable_throwsUnavailableEvenForKnownIsbn() {
        ReviewsService service = new ReviewsService(catalog, Duration.ZERO, false);

        assertThrows(PartnerTemporarilyUnavailableException.class, () -> service.ratingFor("978-966-01-0001-1"));
    }
}