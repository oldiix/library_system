package com.example.partner;

import com.example.common.dto.BookRating;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class ReviewsService {

    private final PartnerCatalog catalog;
    private final Duration delay;
    private final boolean available;

    public ReviewsService(PartnerCatalog catalog,
                          @Value("${partner.reviews.delay:1s}") Duration delay,
                          @Value("${partner.reviews.available:true}") boolean available) {
        this.catalog = catalog;
        this.delay = delay;
        this.available = available;
    }

    public BookRating ratingFor(String isbn) {
        SimulatedLatency.pause(delay);
        if (!available) {
            throw new PartnerTemporarilyUnavailableException("Сервіс відгуків тимчасово недоступний");
        }
        PartnerBook book = catalog.findByIsbn(isbn);
        log.info("Рейтинг для {} «{}»: {} ({} відгуків)",
                isbn, book.title(), book.ratingAverage(), book.reviewsCount());
        return new BookRating(book.ratingAverage(), book.reviewsCount(), true);
    }
}