package com.example.library_system.controller;

import com.example.library_system.dto.StockReportResponse;
import com.example.library_system.service.StockReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StockReportControllerTest {

    @Mock
    private StockReportService stockReportService;

    @Test
    void findAll_reportsExist_returnsThemAsJson() throws Exception {

        when(stockReportService.findAll()).thenReturn(List.of(new StockReportResponse(
                1L, "weekly", LocalDate.of(2026, 10, 26), 3, 1, "Forest Song",
                LocalDateTime.of(2026, 10, 26, 9, 0))));

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StockReportController(stockReportService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        ResultActions result = mockMvc.perform(get("/api/reports/stock"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportName").value("weekly"))
                .andExpect(jsonPath("$[0].threshold").value(3))
                .andExpect(jsonPath("$[0].booksCount").value(1))
                .andExpect(jsonPath("$[0].reportDate").value("2026-10-26"))
                .andExpect(jsonPath("$[0].generatedAt").value("2026-10-26T09:00:00"));
    }
}