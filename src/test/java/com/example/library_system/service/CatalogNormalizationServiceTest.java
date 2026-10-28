package com.example.library_system.service;

import com.example.library_system.entity.Book;
import com.example.library_system.repository.AuthorRepository;
import com.example.library_system.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogNormalizationServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private CatalogNormalizationService catalogNormalizationService;

    @Test
    void normalizeCatalog_moreMessyRecordsThanBatchSize_stopsAfterBatchSizeChanges() {

        Book first = bookWithTitle("  Перша ");
        Book second = bookWithTitle(" Друга  ");
        Book third = bookWithTitle("  Третя  ");
        when(bookRepository.findAll()).thenReturn(List.of(first, second, third));
        when(authorRepository.findAll()).thenReturn(List.of());

        NormalizationResult result = catalogNormalizationService.normalizeCatalog(2);

        assertThat(result.booksUpdated()).isEqualTo(2);
        assertThat(first.getTitle()).isEqualTo("Перша");
        assertThat(second.getTitle()).isEqualTo("Друга");
        assertThat(third.getTitle()).isEqualTo("  Третя  ");
    }

    @Test
    void normalizeCatalog_nonPositiveBatchSize_throwsClearErrorWithoutTouchingData() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> catalogNormalizationService.normalizeCatalog(0));


        assertThat(exception.getMessage()).contains("batchSize", "0");
        verifyNoInteractions(bookRepository, authorRepository);
    }

    private static Book bookWithTitle(String title) {
        Book book = new Book();
        book.setTitle(title);
        return book;
    }
}