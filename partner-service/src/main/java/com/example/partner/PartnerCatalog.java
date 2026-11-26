package com.example.partner;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class PartnerCatalog {

    private final Map<String, PartnerBook> books = Map.of(
            "978-966-01-0001-1", new PartnerBook("978-966-01-0001-1", "Кобзар",
                    4.8, 312, new BigDecimal("420.00"), true, 10),
            "978-966-01-0002-8", new PartnerBook("978-966-01-0002-8", "Лісова пісня",
                    4.6, 154, new BigDecimal("280.00"), true, 5),
            "978-966-01-0003-5", new PartnerBook("978-966-01-0003-5", "Заповіт",
                    4.9, 98, new BigDecimal("190.00"), false, 0));

    public PartnerBook findByIsbn(String isbn) {
        PartnerBook book = isbn == null ? null : books.get(isbn.trim());
        if (book == null) {
            throw new BookNotInCatalogException(isbn);
        }
        return book;
    }
}