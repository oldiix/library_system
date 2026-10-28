package com.example.library_system.service;

import com.example.library_system.entity.Book;
import com.example.library_system.entity.StockReport;
import com.example.library_system.repository.BookRepository;
import com.example.library_system.repository.StockReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({StockReportService.class, StockReportServiceIntegrationTest.FixedClockConfig.class})
class StockReportServiceIntegrationTest {

    @Autowired
    private StockReportService stockReportService;

    @Autowired
    private StockReportRepository stockReportRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void generateReport_runTwiceSameDay_oneReportAndSecondRunChangesNothing() {

        StockReport first = stockReportService.generateReport("daily", 3, 20);
        flushAndClear();
        StockReport second = stockReportService.generateReport("daily", 3, 20);
        flushAndClear();

        assertThat(stockReportRepository.count()).isEqualTo(1);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getGeneratedAt()).isEqualTo(first.getGeneratedAt());

        StockReport stored = stockReportRepository.findById(first.getId()).orElseThrow();
        assertThat(stored.getReportDate()).isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(stored.getBooksCount()).isEqualTo(1);
        assertThat(stored.getBookTitles()).isEqualTo("Лісова пісня");
    }

    @Test
    void generateReport_stockChangedSameDay_updatesExistingReportInsteadOfDuplicating() {

        StockReport first = stockReportService.generateReport("daily", 3, 20);
        flushAndClear();
        Book kobzar = bookRepository.findById(1L).orElseThrow();
        kobzar.setAvailableCopies(2);
        flushAndClear();

        StockReport updated = stockReportService.generateReport("daily", 3, 20);
        flushAndClear();


        assertThat(stockReportRepository.count()).isEqualTo(1);
        assertThat(updated.getId()).isEqualTo(first.getId());
        assertThat(updated.getBooksCount()).isEqualTo(2);
        assertThat(updated.getBookTitles()).isEqualTo("Кобзар; Лісова пісня");
    }

    @Test
    void generateReport_differentReportNamesSameDay_storedAsSeparateReports() {

        stockReportService.generateReport("daily", 1, 20);
        stockReportService.generateReport("weekly", 3, 20);
        flushAndClear();

        assertThat(stockReportRepository.count()).isEqualTo(2);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-10-26T06:00:00Z"), ZoneId.of("Europe/Kyiv"));
        }
    }
}