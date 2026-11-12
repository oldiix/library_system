package com.example.library_system.aop;

import com.example.library_system.audit.AuditEntry;
import com.example.library_system.audit.AuditTrail;
import com.example.library_system.dto.BookDto;
import com.example.library_system.entity.Author;
import com.example.library_system.entity.Book;
import com.example.library_system.mapper.BookMapper;
import com.example.library_system.repository.AuthorRepository;
import com.example.library_system.repository.BookRepository;
import com.example.library_system.service.BookService;
import com.example.library_system.notification.BookNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelfInvocationTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookNotificationService bookNotificationService;

    private AuditTrail auditTrail;
    private BookService bookServiceProxy;
    private Author shevchenko;

    @BeforeEach
    void setUp() {
        BookService target = new BookService(bookRepository, authorRepository, Mappers.getMapper(BookMapper.class), bookNotificationService);

        auditTrail = new AuditTrail();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new AuditAspect(auditTrail,
                Clock.fixed(Instant.parse("2026-10-22T10:00:00Z"), ZoneOffset.UTC)));
        factory.addAspect(new NormalizeInputAspect());
        bookServiceProxy = factory.getProxy();

        shevchenko = new Author();
        shevchenko.setId(1L);
        shevchenko.setFirstName("Тарас");
        shevchenko.setLastName("Шевченко");
    }

    @Test
    void create_calledFromOutsideThroughProxy_aspectsAreApplied() {

        when(authorRepository.findById(1L)).thenReturn(Optional.of(shevchenko));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        BookDto request = new BookDto();
        request.setTitle("   Заповіт   ");
        request.setAvailableCopies(1);
        request.setAuthorId(1L);

        bookServiceProxy.create(request);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Заповіт");

        assertThat(auditTrail.findAll())
                .extracting(AuditEntry::action)
                .containsExactly("BOOK_CREATED");
    }

    @Test
    void copy_callsCreateThroughThis_aspectsOnCreateAreSilentlyBypassed() {

        Book original = new Book();
        original.setId(1L);
        original.setTitle("  Кобзар");
        original.setAvailableCopies(1);
        original.setAuthor(shevchenko);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(original));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(shevchenko));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookDto result = bookServiceProxy.copy(1L);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(result).isNotNull();

        assertThat(captor.getValue().getTitle()).isEqualTo("  Кобзар (копія)");

        assertThat(auditTrail.findAll()).isEmpty();
    }
}