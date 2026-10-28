package com.example.library_system.controller;

import com.example.library_system.dto.StockReportResponse;
import com.example.library_system.service.StockReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports/stock")
@RequiredArgsConstructor
public class StockReportController {

    private final StockReportService stockReportService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<StockReportResponse> findAll() {
        return stockReportService.findAll();
    }
}