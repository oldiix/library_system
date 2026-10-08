package com.example.library_system.service;

import com.example.library_system.dto.BookDto;
import com.example.library_system.entity.Author;
import com.example.library_system.entity.Book;
import com.example.library_system.mapper.BookMapper;
import com.example.library_system.repository.AuthorRepository;
import com.example.library_system.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;

    public List<BookDto> findAll() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public BookDto findById(Long id) {
        return bookMapper.toDto(getBookOrThrow(id));
    }

    @Transactional
    public BookDto create(BookDto dto) {
        Book book = bookMapper.toEntity(dto);
        book.setAuthor(getAuthorOrThrow(dto.getAuthorId()));
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Transactional
    public BookDto update(Long id, BookDto dto) {
        Book book = getBookOrThrow(id);
        bookMapper.updateEntity(dto, book);
        book.setAuthor(getAuthorOrThrow(dto.getAuthorId()));
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Книгу з id " + id + " не знайдено");
        }
        bookRepository.deleteById(id);
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