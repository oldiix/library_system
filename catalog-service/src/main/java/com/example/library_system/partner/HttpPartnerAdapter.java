package com.example.library_system.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import com.example.library_system.config.HttpClientsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class HttpPartnerAdapter implements RatingLookup, OfferLookup, DiscountLookup {

    private final RestClient restClient;

    public HttpPartnerAdapter(@Qualifier(HttpClientsConfig.PARTNER_REST_CLIENT) RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public BookRating findRating(String isbn) {
        return call(isbn, "rating", BookRating.class);
    }

    @Override
    public StoreOffer findOffer(String isbn) {
        return call(isbn, "offer", StoreOffer.class);
    }

    @Override
    public BookDiscount findDiscount(String isbn) {
        return call(isbn, "discount", BookDiscount.class);
    }

    private <T> T call(String isbn, String resource, Class<T> type) {
        try {
            return restClient.get()
                    .uri("/api/partner/books/{isbn}/{resource}", isbn, resource)
                    .retrieve()
                    .body(type);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new PartnerDataNotFoundException("Партнер не знає книги з ISBN " + isbn, exception);
        } catch (RestClientException exception) {
            log.warn("HTTP-виклик partner-service ({} для {}) не вдався: {}", resource, isbn, exception.getMessage());
            throw new PartnerUnavailableException("Партнер тимчасово недоступний", exception);
        }
    }
}