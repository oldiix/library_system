package com.example.library_system.controller;

import com.example.library_system.dto.PartnerRatingResponse;
import com.example.library_system.dto.PurchaseQuoteResponse;
import com.example.library_system.service.PartnerInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class PartnerInfoController {

    private final PartnerInfoService partnerInfoService;

    @GetMapping("/{id}/partner-rating")
    public PartnerRatingResponse partnerRating(@PathVariable Long id) {
        return partnerInfoService.partnerRating(id);
    }

    @GetMapping("/{id}/purchase-quote")
    public PurchaseQuoteResponse purchaseQuote(@PathVariable Long id) {
        return partnerInfoService.purchaseQuote(id);
    }
}