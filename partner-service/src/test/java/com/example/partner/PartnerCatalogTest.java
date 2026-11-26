package com.example.partner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartnerCatalogTest {

    private final PartnerCatalog catalog = new PartnerCatalog();

    @Test
    void findByIsbn_knownIsbn_returnsBook() {
        PartnerBook book = catalog.findByIsbn("978-966-01-0001-1");

        assertThat(book.title()).isEqualTo("Кобзар");
        assertThat(book.discountPercent()).isEqualTo(10);
    }

    @Test
    void findByIsbn_isbnWithSpaces_isTrimmed() {
        assertThat(catalog.findByIsbn(" 978-966-01-0002-8 ").title()).isEqualTo("Лісова пісня");
    }

    @Test
    void findByIsbn_unknownIsbn_throwsNotInCatalog() {
        BookNotInCatalogException exception = assertThrows(BookNotInCatalogException.class,
                () -> catalog.findByIsbn("978-0-00-000000-0"));

        assertThat(exception.getMessage()).contains("978-0-00-000000-0");
    }

    @Test
    void findByIsbn_noIsbn_throwsNotInCatalog() {
        assertThrows(BookNotInCatalogException.class, () -> catalog.findByIsbn(null));
    }
}