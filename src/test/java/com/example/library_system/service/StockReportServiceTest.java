package com.example.library_system.service;

import com.example.library_system.entity.Book;
import com.example.library_system.entity.StockReport;
import com.example.library_system.repository.BookRepository;
import com.example.library_system.repository.StockReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReportServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private StockReportRepository stockReportRepository;

    private StockReportService stockReportService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-26T06:00:00Z"), ZoneId.of("Europe/Kyiv"));
        stockReportService = new StockReportService(bookRepository, stockReportRepository, clock);
    }

    @Test
    void generateReport_moreBooksThanMaxTitles_listsOnlyFirstTitlesAndCountsTheRest() {

        when(bookRepository.findByAvailableCopiesLessThanEqualOrderByTitleAsc(1))
                .thenReturn(List.of(book("Альфа"), book("Бета"), book("Гамма")));
        when(stockReportRepository.findByReportNameAndReportDate(anyString(), any()))
                .thenReturn(Optional.empty());
        when(stockReportRepository.save(any(StockReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockReport report = stockReportService.generateReport("daily", 1, 2);

        assertThat(report.getBooksCount()).isEqualTo(3);
        assertThat(report.getBookTitles()).isEqualTo("Альфа; Бета; … ще 1");
    }

    @Test
    void generateReport_negativeThreshold_throwsClearErrorWithoutTouchingData() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stockReportService.generateReport("daily", -1, 20));

        assertThat(exception.getMessage()).contains("Поріг", "-1");
        verifyNoInteractions(bookRepository, stockReportRepository);
    }

    private static Book book(String title) {
        Book book = new Book();
        book.setTitle(title);
        return book;
    }
}