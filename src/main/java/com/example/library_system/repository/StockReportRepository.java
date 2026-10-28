package com.example.library_system.repository;

import com.example.library_system.entity.StockReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockReportRepository extends JpaRepository<StockReport, Long> {

    Optional<StockReport> findByReportNameAndReportDate(String reportName, LocalDate reportDate);

    List<StockReport> findAllByOrderByReportDateDescReportNameAsc();
}