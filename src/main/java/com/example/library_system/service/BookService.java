package com.example.library_system.service;

import com.example.library_system.aop.Audited;
import com.example.library_system.aop.FeatureToggle;
import com.example.library_system.aop.NormalizeInput;
import com.example.library_system.dto.BookDto;
import com.example.library_system.entity.Author;
import com.example.library_system.entity.Book;
import com.example.library_system.mapper.BookMapper;
import com.example.library_system.notification.BookNotificationService;
import com.example.library_system.repository.AuthorRepository;
import com.example.library_system.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;
    private final BookNotificationService bookNotificationService;

    public List<BookDto> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public BookDto findById(Long id) {
        return bookMapper.toDto(getBookOrThrow(id));
    }

    @NormalizeInput
    @Audited(action = "BOOK_CREATED")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookDto create(BookDto dto) {
        Book book = bookMapper.toEntity(dto);
        book.setAuthor(getAuthorOrThrow(dto.getAuthorId()));
        BookDto created = bookMapper.toDto(bookRepository.save(book));
        notifyReadersSafely(created);
        return created;
    }

    @NormalizeInput
    @Audited(action = "BOOK_UPDATED")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookDto update(Long id, BookDto dto) {
        Book book = getBookOrThrow(id);
        Author author = getAuthorOrThrow(dto.getAuthorId());
        bookMapper.updateEntity(dto, book);
        book.setAuthor(author);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookDto copy(Long id) {
        BookDto copy = bookMapper.toDto(getBookOrThrow(id));
        copy.setId(null);
        copy.setTitle(copy.getTitle() + " (копія)");
        return this.create(copy);
    }

    @Audited(action = "BOOK_DELETED")
    @FeatureToggle("book-deletion")
    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Книгу з id " + id + " не знайдено");
        }
        bookRepository.deleteById(id);
    }

    private void notifyReadersSafely(BookDto book) {
        try {
            bookNotificationService.notifyNewBook(book.getTitle(), book.getAuthorName());
        } catch (TaskRejectedException exception) {

            log.warn("Пул асинхронних задач переповнений — сповіщення про книгу «{}» пропущено",
                    book.getTitle());
        }
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Книгу з id " + id + " не знайдено"));
    }

    private Author getAuthorOrThrow(Long authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Автора з id " + authorId + " не знайдено"));
    }
}