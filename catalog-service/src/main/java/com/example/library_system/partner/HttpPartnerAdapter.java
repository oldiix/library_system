package com.example.library_system.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.BookRating;
import com.example.common.dto.StoreOffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class HttpPartnerAdapter implements RatingLookup, OfferLookup, DiscountLookup {

    private final PartnerHttpApi partnerHttpApi;

    @Override
    public BookRating findRating(String isbn) {
        return call(isbn, "rating", () -> partnerHttpApi.rating(isbn));
    }

    @Override
    public StoreOffer findOffer(String isbn) {
        return call(isbn, "offer", () -> partnerHttpApi.offer(isbn));
    }

    @Override
    public BookDiscount findDiscount(String isbn) {
        return call(isbn, "discount", () -> partnerHttpApi.discount(isbn));
    }

    private <T> T call(String isbn, String resource, Supplier<T> request) {
        try {
            return request.get();
        } catch (HttpClientErrorException.NotFound exception) {
            throw new PartnerDataNotFoundException("Партнер не знає книги з ISBN " + isbn, exception);
        } catch (RestClientException exception) {
            log.warn("HTTP-виклик partner-service ({} для {}) не вдався: {}", resource, isbn, exception.getMessage());
            throw new PartnerUnavailableException("Партнер тимчасово недоступний", exception);
        }
    }
}