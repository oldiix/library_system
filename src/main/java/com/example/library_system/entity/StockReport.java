package com.example.library_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_reports")
@Getter
@Setter
@NoArgsConstructor
public class StockReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_name", nullable = false)
    private String reportName;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "threshold", nullable = false)
    private Integer threshold;

    @Column(name = "books_count", nullable = false)
    private Integer booksCount;

    @Column(name = "book_titles", length = 2000)
    private String bookTitles;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}