package com.example.library_system.service;

import com.example.library_system.dto.AuthorDto;
import com.example.library_system.entity.Author;
import com.example.library_system.mapper.AuthorMapper;
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
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final AuthorMapper authorMapper;

    public List<AuthorDto> findAll() {
        return authorRepository.findAll()
                .stream()
                .map(authorMapper::toDto)
                .toList();
    }

    public AuthorDto findById(Long id) {
        Author author = getAuthorOrThrow(id);
        return authorMapper.toDto(author);
    }

    @Transactional
    public AuthorDto create(AuthorDto dto) {
        Author author = authorMapper.toEntity(dto);
        Author saved = authorRepository.save(author);
        return authorMapper.toDto(saved);
    }

    @Transactional
    public AuthorDto update(Long id, AuthorDto dto) {
        Author author = getAuthorOrThrow(id);
        authorMapper.updateEntity(dto, author);
        Author saved = authorRepository.save(author);
        return authorMapper.toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Автора з id " + id + " не знайдено");
        }
        if (bookRepository.existsByAuthorId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Неможливо видалити автора: у нього є книги");
        }
        authorRepository.deleteById(id);
    }

    private Author getAuthorOrThrow(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Автора з id " + id + " не знайдено"));
    }
}