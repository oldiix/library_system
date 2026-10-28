package com.example.library_system.service;

import com.example.library_system.dto.StockReportResponse;
import com.example.library_system.entity.Book;
import com.example.library_system.entity.StockReport;
import com.example.library_system.repository.BookRepository;
import com.example.library_system.repository.StockReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReportService {

    static final int MAX_TITLES_LENGTH = 2000;

    private final BookRepository bookRepository;
    private final StockReportRepository stockReportRepository;
    private final Clock clock;

    @Transactional
    public StockReport generateReport(String reportName, int threshold, int maxTitles) {
        if (reportName == null || reportName.isBlank()) {
            throw new IllegalArgumentException("Назва звіту не може бути порожньою");
        }
        if (threshold < 0) {
            throw new IllegalArgumentException("Поріг не може бути від'ємним, отримано: " + threshold);
        }
        if (maxTitles <= 0) {
            throw new IllegalArgumentException("maxTitles має бути більшим за 0, отримано: " + maxTitles);
        }

        LocalDate reportDate = LocalDate.now(clock);
        List<Book> lowStockBooks = bookRepository.findByAvailableCopiesLessThanEqualOrderByTitleAsc(threshold);
        String titles = summarizeTitles(lowStockBooks, maxTitles);

        StockReport report = stockReportRepository.findByReportNameAndReportDate(reportName, reportDate)
                .orElseGet(StockReport::new);

        if (report.getId() != null && hasSameContent(report, threshold, lowStockBooks.size(), titles)) {
            log.info("Звіт «{}» за {} уже актуальний — нічого не змінено", reportName, reportDate);
            return report;
        }

        report.setReportName(reportName);
        report.setReportDate(reportDate);
        report.setThreshold(threshold);
        report.setBooksCount(lowStockBooks.size());
        report.setBookTitles(titles);
        report.setGeneratedAt(LocalDateTime.now(clock));
        StockReport saved = stockReportRepository.save(report);

        log.info("Звіт «{}» за {}: книг із запасом ≤ {} — {}",
                reportName, reportDate, threshold, lowStockBooks.size());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<StockReportResponse> findAll() {
        return stockReportRepository.findAllByOrderByReportDateDescReportNameAsc().stream()
                .map(StockReportResponse::from)
                .toList();
    }

    private static boolean hasSameContent(StockReport report, int threshold, int booksCount, String titles) {
        return Objects.equals(report.getThreshold(), threshold)
                && Objects.equals(report.getBooksCount(), booksCount)
                && Objects.equals(report.getBookTitles(), titles);
    }

    private static String summarizeTitles(List<Book> books, int maxTitles) {
        String summary = books.stream()
                .limit(maxTitles)
                .map(Book::getTitle)
                .collect(Collectors.joining("; "));
        if (books.size() > maxTitles) {
            summary += "; … ще " + (books.size() - maxTitles);
        }
        return summary.length() > MAX_TITLES_LENGTH ? summary.substring(0, MAX_TITLES_LENGTH) : summary;
    }
}