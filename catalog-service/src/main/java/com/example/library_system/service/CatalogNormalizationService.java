package com.example.library_system.service;

import com.example.library_system.aop.Normalizable;
import com.example.library_system.entity.Author;
import com.example.library_system.entity.Book;
import com.example.library_system.repository.AuthorRepository;
import com.example.library_system.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogNormalizationService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;


    @Transactional
    public NormalizationResult normalizeCatalog(int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize має бути більшим за 0, отримано: " + batchSize);
        }

        int booksUpdated = 0;
        for (Book book : bookRepository.findAll()) {
            if (booksUpdated >= batchSize) {
                break;
            }
            if (normalize(book)) {
                booksUpdated++;
            }
        }

        int authorsUpdated = 0;
        int remaining = batchSize - booksUpdated;
        for (Author author : authorRepository.findAll()) {
            if (authorsUpdated >= remaining) {
                break;
            }
            if (normalize(author)) {
                authorsUpdated++;
            }
        }

        NormalizationResult result = new NormalizationResult(booksUpdated, authorsUpdated);
        log.info("Впорядкування каталогу: оновлено книг — {}, авторів — {}",
                result.booksUpdated(), result.authorsUpdated());
        return result;
    }

    private boolean normalize(Book book) {
        String title = Normalizable.normalizeText(book.getTitle());
        String isbn = Normalizable.normalizeOptionalText(book.getIsbn());
        if (Objects.equals(title, book.getTitle()) && Objects.equals(isbn, book.getIsbn())) {
            return false;
        }
        book.setTitle(title);
        book.setIsbn(isbn);
        return true;
    }

    private boolean normalize(Author author) {
        String firstName = Normalizable.normalizeText(author.getFirstName());
        String lastName = Normalizable.normalizeText(author.getLastName());
        String country = Normalizable.normalizeOptionalText(author.getCountry());
        if (Objects.equals(firstName, author.getFirstName())
                && Objects.equals(lastName, author.getLastName())
                && Objects.equals(country, author.getCountry())) {
            return false;
        }
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setCountry(country);
        return true;
    }
}