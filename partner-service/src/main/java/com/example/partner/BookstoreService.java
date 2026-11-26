package com.example.partner;

import com.example.common.dto.BookDiscount;
import com.example.common.dto.StoreOffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class BookstoreService {

    private final PartnerCatalog catalog;
    private final Duration delay;
    private final boolean available;

    public BookstoreService(PartnerCatalog catalog,
                            @Value("${partner.bookstore.delay:1s}") Duration delay,
                            @Value("${partner.bookstore.available:true}") boolean available) {
        this.catalog = catalog;
        this.delay = delay;
        this.available = available;
    }

    public StoreOffer offerFor(String isbn) {
        PartnerBook book = findAvailable(isbn);
        log.info("Пропозиція для {} «{}»: {} грн, в наявності: {}",
                isbn, book.title(), book.priceUah(), book.inStock());
        return new StoreOffer(book.priceUah(), book.inStock(), true);
    }

    public BookDiscount discountFor(String isbn) {
        PartnerBook book = findAvailable(isbn);
        log.info("Знижка для {} «{}»: {}%", isbn, book.title(), book.discountPercent());
        return new BookDiscount(book.isbn(), book.discountPercent());
    }

    private PartnerBook findAvailable(String isbn) {
        SimulatedLatency.pause(delay);
        if (!available) {
            throw new PartnerTemporarilyUnavailableException("Книгарня-партнер тимчасово недоступна");
        }
        return catalog.findByIsbn(isbn);
    }
}