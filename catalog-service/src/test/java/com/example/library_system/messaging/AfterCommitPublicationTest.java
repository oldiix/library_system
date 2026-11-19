package com.example.library_system.messaging;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.LibraryEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringJUnitConfig(AfterCommitPublicationTest.TestConfig.class)
class AfterCommitPublicationTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private LibraryEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private BookAction bookAction;

    @BeforeEach
    void resetBroker() {
        Mockito.reset(amqpTemplate);
    }

    @Test
    void successfulAction_eventIsSentOnlyAfterCommit() {
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.bookCreated(1L, "Кобзар", "Тарас Шевченко");
            verifyNoInteractions(amqpTemplate);
        });

        verify(amqpTemplate, times(1)).convertAndSend(
                eq(LibraryEvents.EXCHANGE), eq(LibraryEvents.BOOK_CREATED), any(BookCreatedEvent.class));
    }

    @Test
    void failedAction_transactionRolledBack_noEventIsSent() {
        assertThrows(IllegalStateException.class, () -> bookAction.createBook(true));

        verifyNoInteractions(amqpTemplate);
    }

    @Test
    void transactionalServiceMethod_committed_exactlyOneEventIsSent() {
        bookAction.createBook(false);

        verify(amqpTemplate, times(1)).convertAndSend(
                eq(LibraryEvents.EXCHANGE), eq(LibraryEvents.BOOK_CREATED), any(BookCreatedEvent.class));
    }

    public static class BookAction {

        private final LibraryEventPublisher eventPublisher;

        public BookAction(LibraryEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @Transactional
        public void createBook(boolean failAfterPublishing) {
            eventPublisher.bookCreated(1L, "Кобзар", "Тарас Шевченко");
            if (failAfterPublishing) {
                throw new IllegalStateException("База даних відмовила");
            }
        }
    }

    @Configuration
    @EnableTransactionManagement
    static class TestConfig {

        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).generateUniqueName(true).build();
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager);
        }

        @Bean
        AmqpTemplate amqpTemplate() {
            return Mockito.mock(AmqpTemplate.class);
        }

        @Bean
        LibraryEventPublisher libraryEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            return new LibraryEventPublisher(applicationEventPublisher,
                    Clock.fixed(Instant.parse("2026-11-19T20:00:00Z"), ZoneOffset.UTC));
        }

        @Bean
        BrokerEventRelay brokerEventRelay(AmqpTemplate amqpTemplate) {
            return new BrokerEventRelay(amqpTemplate);
        }

        @Bean
        BookAction bookAction(LibraryEventPublisher libraryEventPublisher) {
            return new BookAction(libraryEventPublisher);
        }
    }
}
