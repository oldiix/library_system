package com.example.library_system.external;

import com.example.library_system.config.AsyncConfig;
import com.example.library_system.config.AsyncProperties;
import com.example.library_system.config.LoggingAsyncExceptionHandler;
import com.example.library_system.dto.BookDetailsResponse;
import com.example.library_system.dto.BookDto;
import com.example.library_system.service.BookDetailsService;
import com.example.library_system.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(ParallelCallsTest.TestConfig.class)
class ParallelCallsTest {

    @Autowired
    private BookDetailsService bookDetailsService;

    @Autowired
    private BookService bookService;

    @Autowired
    private RendezvousGateways gateways;

    @Test
    void getDetails_twoAsyncCalls_runConcurrentlyInOwnExecutorAndAreCombined() {

        BookDto kobzar = new BookDto();
        kobzar.setId(1L);
        kobzar.setTitle("Кобзар");
        when(bookService.findById(1L)).thenReturn(kobzar);

        BookDetailsResponse details = bookDetailsService.getDetails(1L);

        assertThat(details.rating().available()).isTrue();
        assertThat(details.offer().available()).isTrue();
        assertThat(details.rating().average()).isEqualTo(4.8);
        assertThat(details.offer().priceUah()).isEqualByComparingTo("250");

        assertThat(gateways.threadNames)
                .hasSize(2)
                .allSatisfy(name -> assertThat(name).startsWith("library-async-"));
    }

    // Мінімальний Spring-контекст

    @Configuration
    @Import({AsyncConfig.class, LoggingAsyncExceptionHandler.class, ExternalCatalogClient.class})
    static class TestConfig {

        @Bean
        AsyncProperties asyncProperties() {
            return new AsyncProperties(2, 4, 10, "library-async-");
        }

        @Bean
        RendezvousGateways rendezvousGateways() {
            return new RendezvousGateways();
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

    static class RendezvousGateways implements ReviewsGateway, BookstoreGateway {

        private final CountDownLatch bothStarted = new CountDownLatch(2);
        final Set<String> threadNames = ConcurrentHashMap.newKeySet();

        @Override
        public BookRating fetchRating(String title) {
            meetTheOtherCall();
            return new BookRating(4.8, 120, true);
        }

        @Override
        public StoreOffer fetchOffer(String title) {
            meetTheOtherCall();
            return new StoreOffer(new BigDecimal("250"), true, true);
        }

        private void meetTheOtherCall() {
            threadNames.add(Thread.currentThread().getName());
            bothStarted.countDown();
            try {
                if (!bothStarted.await(2, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Виклики виконуються не одночасно");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }
}