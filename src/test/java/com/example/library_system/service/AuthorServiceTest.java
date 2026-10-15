package com.example.library_system.service;

import com.example.library_system.dto.AuthorDto;
import com.example.library_system.entity.Author;
import com.example.library_system.mapper.AuthorMapper;
import com.example.library_system.repository.AuthorRepository;
import com.example.library_system.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @Spy
    private AuthorMapper authorMapper = Mappers.getMapper(AuthorMapper.class);

    @InjectMocks
    private AuthorService authorService;

    // findAll

    @Test
    void findAll_authorsExist_returnsAllAuthorsAsDtos() {

        Author shevchenko = createAuthor(1L, "Тарас", "Шевченко", "Україна");
        Author ukrainka = createAuthor(2L, "Леся", "Українка", "Україна");
        when(authorRepository.findAll()).thenReturn(List.of(shevchenko, ukrainka));

        List<AuthorDto> result = authorService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getLastName()).isEqualTo("Шевченко");
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getLastName()).isEqualTo("Українка");
    }

    @Test
    void findAll_noAuthorsInRepository_returnsEmptyList() {

        when(authorRepository.findAll()).thenReturn(List.of());

        List<AuthorDto> result = authorService.findAll();

        assertThat(result).isEmpty();
    }

    // findById

    @Test
    void findById_existingId_returnsAuthorDto() {

        Author author = createAuthor(1L, "Тарас", "Шевченко", "Україна");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        AuthorDto result = authorService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("Тарас");
        assertThat(result.getLastName()).isEqualTo("Шевченко");
        assertThat(result.getCountry()).isEqualTo("Україна");
    }

    @Test
    void findById_nonExistingId_throwsNotFound() {

        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authorService.findById(99L));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Автора з id 99 не знайдено");
    }

    // create

    @Test
    void create_validDto_savesAuthorAndReturnsDtoWithGeneratedId() {

        AuthorDto request = createAuthorDto(null, "Іван", "Франко", "Україна");
        Author savedAuthor = createAuthor(3L, "Іван", "Франко", "Україна");
        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        AuthorDto result = authorService.create(request);

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        Author authorPassedToSave = captor.getValue();
        assertThat(authorPassedToSave.getFirstName()).isEqualTo("Іван");
        assertThat(authorPassedToSave.getLastName()).isEqualTo("Франко");
        assertThat(authorPassedToSave.getCountry()).isEqualTo("Україна");

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getLastName()).isEqualTo("Франко");
    }

    @Test
    void create_requestContainsClientId_ignoresIdAndCreatesNewAuthor() {

        AuthorDto request = createAuthorDto(1L, "Іван", "Франко", "Україна");
        Author savedAuthor = createAuthor(3L, "Іван", "Франко", "Україна");
        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        AuthorDto result = authorService.create(request);

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(result.getId()).isEqualTo(3L);
    }

    // update

    @Test
    void update_existingAuthor_updatesFieldsAndSaves() {

        Author existing = createAuthor(3L, "Іван", "Франко", "Україна");
        AuthorDto request = createAuthorDto(null, "Іван", "Франко", "Австро-Угорщина");
        when(authorRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthorDto result = authorService.update(3L, request);

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        Author authorPassedToSave = captor.getValue();
        assertThat(authorPassedToSave).isSameAs(existing);
        assertThat(authorPassedToSave.getCountry()).isEqualTo("Австро-Угорщина");

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getCountry()).isEqualTo("Австро-Угорщина");
    }

    @Test
    void update_nonExistingAuthor_throwsNotFoundAndDoesNotSave() {

        AuthorDto request = createAuthorDto(null, "Іван", "Франко", "Україна");
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authorService.update(99L, request));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(authorRepository, never()).save(any());
    }

    @Test
    void update_requestContainsDifferentId_keepsOriginalId() {

        Author existing = createAuthor(3L, "Іван", "Франко", "Україна");
        AuthorDto request = createAuthorDto(55L, "Іван", "Франко", "Україна");
        when(authorRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authorService.update(3L, request);

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(3L);
    }

    // delete

    @Test
    void delete_existingAuthorWithoutBooks_deletesAuthor() {

        when(authorRepository.existsById(3L)).thenReturn(true);
        when(bookRepository.existsByAuthorId(3L)).thenReturn(false);

        authorService.delete(3L);

        verify(authorRepository).deleteById(3L);
    }

    @Test
    void delete_nonExistingAuthor_throwsNotFoundAndDoesNotDelete() {

        when(authorRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authorService.delete(99L));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Автора з id 99 не знайдено");
        verify(authorRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_authorHasBooks_throwsConflictAndDoesNotDelete() {

        when(authorRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.existsByAuthorId(1L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authorService.delete(1L));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Неможливо видалити автора: у нього є книги");
        verify(authorRepository, never()).deleteById(anyLong());
    }

    private static Author createAuthor(Long id, String firstName, String lastName, String country) {
        Author author = new Author();
        author.setId(id);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setCountry(country);
        return author;
    }

    private static AuthorDto createAuthorDto(Long id, String firstName, String lastName, String country) {
        AuthorDto dto = new AuthorDto();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setCountry(country);
        return dto;
    }
}