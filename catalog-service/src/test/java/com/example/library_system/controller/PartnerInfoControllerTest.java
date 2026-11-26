package com.example.library_system.controller;

import com.example.library_system.dto.PurchaseQuoteResponse;
import com.example.library_system.partner.PartnerDataNotFoundException;
import com.example.library_system.partner.PartnerUnavailableException;
import com.example.library_system.service.PartnerInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PartnerInfoControllerTest {

    @Mock
    private PartnerInfoService partnerInfoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PartnerInfoController(partnerInfoService))
                .setControllerAdvice(new PartnerErrorHandler())
                .build();
    }

    @Test
    void purchaseQuote_partnerResponds_returns200() throws Exception {
        when(partnerInfoService.purchaseQuote(1L)).thenReturn(new PurchaseQuoteResponse(
                1L, "Кобзар", "978-966-01-0001-1", new BigDecimal("420.00"), 10, new BigDecimal("378.00"), true));

        mockMvc.perform(get("/api/books/1/purchase-quote"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceAfterDiscountUah").value(378.00))
                .andExpect(jsonPath("$.discountPercent").value(10));
    }

    @Test
    void partnerDoesNotKnowBook_returns404() throws Exception {
        when(partnerInfoService.partnerRating(2L))
                .thenThrow(new PartnerDataNotFoundException("Партнер не знає книги з ISBN 978-0-00-000000-0"));

        mockMvc.perform(get("/api/books/2/partner-rating"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void partnerUnavailable_returns503() throws Exception {
        when(partnerInfoService.purchaseQuote(1L))
                .thenThrow(new PartnerUnavailableException("Партнер тимчасово недоступний", new IOException("timeout")));

        mockMvc.perform(get("/api/books/1/purchase-quote"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }
}