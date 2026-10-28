package com.example.library_system.dto;

import com.example.library_system.entity.StockReport;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockReportResponse(Long id,
                                  String reportName,
                                  LocalDate reportDate,
                                  int threshold,
                                  int booksCount,
                                  String bookTitles,
                                  LocalDateTime generatedAt) {

    public static StockReportResponse from(StockReport report) {
        return new StockReportResponse(
                report.getId(),
                report.getReportName(),
                report.getReportDate(),
                report.getThreshold(),
                report.getBooksCount(),
                report.getBookTitles(),
                report.getGeneratedAt());
    }
}