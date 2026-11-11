package com.example.library_system.service;

import com.example.library_system.entity.Author;
import com.example.library_system.entity.Book;
import com.example.library_system.repository.AuthorRepository;
import com.example.library_system.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CatalogNormalizationService.class)
class CatalogNormalizationServiceIntegrationTest {

    @Autowired
    private CatalogNormalizationService catalogNormalizationService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void normalizeCatalog_runTwiceInARow_secondRunChangesNothing() {

        Author shevchenko = authorRepository.findById(1L).orElseThrow();
        shevchenko.setLastName("  Шевченко   ");
        Book messyBook = new Book();
        messyBook.setTitle("   Кобзар    (копія)  ");
        messyBook.setIsbn("    ");
        messyBook.setAvailableCopies(1);
        messyBook.setAuthor(shevchenko);
        bookRepository.save(messyBook);
        flushAndClear();

        NormalizationResult firstRun = catalogNormalizationService.normalizeCatalog(100);
        flushAndClear();
        NormalizationResult secondRun = catalogNormalizationService.normalizeCatalog(100);
        flushAndClear();

        assertThat(firstRun.booksUpdated()).isEqualTo(1);
        assertThat(firstRun.authorsUpdated()).isEqualTo(1);

        assertThat(secondRun.total()).isZero();

        Book reloadedBook = bookRepository.findById(messyBook.getId()).orElseThrow();
        assertThat(reloadedBook.getTitle()).isEqualTo("Кобзар (копія)");
        assertThat(reloadedBook.getIsbn()).isNull();
        assertThat(authorRepository.findById(1L).orElseThrow().getLastName()).isEqualTo("Шевченко");
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}