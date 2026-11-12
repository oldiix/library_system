package com.example.partner;

import com.example.common.dto.StoreOffer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookstoreServiceTest {

    @Test
    void offerFor_bookstoreAvailable_returnsStableOfferInExpectedRange() {

        BookstoreService service = new BookstoreService(Duration.ZERO, true);

        StoreOffer first = service.offerFor("Кобзар");
        StoreOffer second = service.offerFor("Кобзар");

        assertThat(first).isEqualTo(second);
        assertThat(first.available()).isTrue();
        assertThat(first.priceUah().intValue()).isBetween(150, 549);
    }

    @Test
    void offerFor_bookstoreUnavailable_throwsServiceUnavailable() {

        BookstoreService service = new BookstoreService(Duration.ZERO, false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.offerFor("Кобзар"));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}