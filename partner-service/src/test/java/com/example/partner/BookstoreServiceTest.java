package com.example.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.StoreOffer;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookstoreServiceTest {

    private final PartnerCatalog catalog = new PartnerCatalog();

    @Test
    void offerFor_knownIsbn_returnsPriceAndStock() {
        BookstoreService service = new BookstoreService(catalog, Duration.ZERO, true);

        StoreOffer offer = service.offerFor("978-966-01-0003-5");

        assertThat(offer.priceUah()).isEqualByComparingTo("190.00");
        assertThat(offer.inStock()).isFalse();
        assertThat(offer.available()).isTrue();
    }

    @Test
    void discountFor_knownIsbn_returnsPercent() {
        BookstoreService service = new BookstoreService(catalog, Duration.ZERO, true);

        BookDiscount discount = service.discountFor("978-966-01-0001-1");

        assertThat(discount).isEqualTo(new BookDiscount("978-966-01-0001-1", 10));
    }

    @Test
    void offerAndDiscount_unknownIsbn_throwNotInCatalog() {
        BookstoreService service = new BookstoreService(catalog, Duration.ZERO, true);

        assertThrows(BookNotInCatalogException.class, () -> service.offerFor("978-0-00-000000-0"));
        assertThrows(BookNotInCatalogException.class, () -> service.discountFor("978-0-00-000000-0"));
    }

    @Test
    void offer_bookstoreUnavailable_throwsUnavailable() {
        BookstoreService service = new BookstoreService(catalog, Duration.ZERO, false);

        assertThrows(PartnerTemporarilyUnavailableException.class, () -> service.offerFor("978-966-01-0001-1"));
    }
}