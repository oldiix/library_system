package com.example.library_system.controller;

import com.example.library_system.config.AsyncConfig;
import com.example.library_system.config.AsyncProperties;
import com.example.library_system.config.LoggingAsyncExceptionHandler;
import com.example.library_system.dto.BookDto;
import com.example.library_system.external.BookRating;
import com.example.library_system.external.BookstoreGateway;
import com.example.library_system.external.ExternalCatalogClient;
import com.example.library_system.external.ReviewsGateway;
import com.example.library_system.external.StoreOffer;
import com.example.library_system.service.BookDetailsService;
import com.example.library_system.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(OverloadHandlingTest.TestConfig.class)
class OverloadHandlingTest {

    @Autowired
    private ThreadPoolTaskExecutor libraryTaskExecutor;

    @Autowired
    private BookDetailsService bookDetailsService;

    @Autowired
    private BookService bookService;

    @Test
    void getDetails_executorSaturated_returns503WithRetryAfterInsteadOf500() throws Exception {

        BookDto kobzar = new BookDto();
        kobzar.setId(1L);
        kobzar.setTitle("Kobzar");
        when(bookService.findById(1L)).thenReturn(kobzar);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new BookDetailsController(bookDetailsService))
                .setControllerAdvice(new OverloadExceptionHandler())
                .build();

        CountDownLatch release = new CountDownLatch(1);
        Runnable blockingTask = () -> {
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        libraryTaskExecutor.execute(blockingTask);   // займаю єдиний потік пулу
        libraryTaskExecutor.execute(blockingTask);   // заповнюю чергу

        try {
            ResultActions result = mockMvc.perform(get("/api/books/1/details"));

            result.andExpect(status().isServiceUnavailable())
                    .andExpect(header().string("Retry-After", "5"))
                    .andExpect(jsonPath("$.status").value(503))
                    .andExpect(jsonPath("$.error").value("Service Unavailable"))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        } finally {
            release.countDown();
        }
    }

    // Мінімальний Spring-контекст з крихітним пулом

    @Configuration
    @Import({AsyncConfig.class, LoggingAsyncExceptionHandler.class, ExternalCatalogClient.class})
    static class TestConfig {

        @Bean
        AsyncProperties asyncProperties() {
            return new AsyncProperties(1, 1, 1, "tiny-async-");
        }

        @Bean
        StubGateways stubGateways() {
            return new StubGateways();
        }

        @Bean
        BookService bookService() {
            return Mockito.mock(BookService.class);
        }

        @Bean
        BookDetailsService bookDetailsService(BookService bookService, ExternalCatalogClient client) {
            return new BookDetailsService(bookService, client, Duration.ofSeconds(5));
        }
    }

    static class StubGateways implements ReviewsGateway, BookstoreGateway {

        @Override
        public BookRating fetchRating(String title) {
            return new BookRating(4.8, 120, true);
        }

        @Override
        public StoreOffer fetchOffer(String title) {
            return new StoreOffer(new BigDecimal("250"), true, true);
        }
    }
}