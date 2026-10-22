package com.example.library_system.service;

import com.example.library_system.dto.BookDto;
import com.example.library_system.entity.Author;
import com.example.library_system.entity.Book;
import com.example.library_system.mapper.BookMapper;
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
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Spy
    private BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    @InjectMocks
    private BookService bookService;

    // findAll

    @Test
    void findAll_booksExist_returnsAllBooksWithAuthorNames() {

        Author shevchenko = createAuthor(1L, "Тарас", "Шевченко");
        Author ukrainka = createAuthor(2L, "Леся", "Українка");
        Book kobzar = createBook(1L, "Кобзар", "978-966-01-0001-1", 1840, 5, shevchenko);
        Book forestSong = createBook(2L, "Лісова пісня", "978-966-01-0002-8", 1911, 3, ukrainka);
        when(bookRepository.findAll()).thenReturn(List.of(kobzar, forestSong));

        List<BookDto> result = bookService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Кобзар");
        assertThat(result.get(0).getAuthorName()).isEqualTo("Тарас Шевченко");
        assertThat(result.get(1).getTitle()).isEqualTo("Лісова пісня");
        assertThat(result.get(1).getAuthorName()).isEqualTo("Леся Українка");
    }

    @Test
    void findAll_noBooksInRepository_returnsEmptyList() {

        when(bookRepository.findAll()).thenReturn(List.of());

        List<BookDto> result = bookService.findAll();

        assertThat(result).isEmpty();
    }

    // findById

    @Test
    void findById_existingId_returnsBookDtoWithAuthorData() {

        Author shevchenko = createAuthor(1L, "Тарас", "Шевченко");
        Book kobzar = createBook(1L, "Кобзар", "978-966-01-0001-1", 1840, 5, shevchenko);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(kobzar));

        BookDto result = bookService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Кобзар");
        assertThat(result.getIsbn()).isEqualTo("978-966-01-0001-1");
        assertThat(result.getPublicationYear()).isEqualTo(1840);
        assertThat(result.getAvailableCopies()).isEqualTo(5);
        assertThat(result.getAuthorId()).isEqualTo(1L);
        assertThat(result.getAuthorName()).isEqualTo("Тарас Шевченко");
    }

    @Test
    void findById_nonExistingId_throwsNotFound() {

        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookService.findById(99L));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Книгу з id 99 не знайдено");
    }

    // create

    @Test
    void create_existingAuthor_savesBookLinkedToAuthor() {

        Author shevchenko = createAuthor(1L, "Тарас", "Шевченко");
        BookDto request = createBookDto(null, "Заповіт", "978-966-01-0003-5", 1845, 4, 1L);
        Book savedBook = createBook(10L, "Заповіт", "978-966-01-0003-5", 1845, 4, shevchenko);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(shevchenko));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookDto result = bookService.create(request);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book bookPassedToSave = captor.getValue();
        assertThat(bookPassedToSave.getId()).isNull();
        assertThat(bookPassedToSave.getTitle()).isEqualTo("Заповіт");
        assertThat(bookPassedToSave.getAvailableCopies()).isEqualTo(4);
        assertThat(bookPassedToSave.getAuthor()).isSameAs(shevchenko);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getAuthorId()).isEqualTo(1L);
        assertThat(result.getAuthorName()).isEqualTo("Тарас Шевченко");
    }

    @Test
    void create_nonExistingAuthor_throwsNotFoundAndDoesNotSave() {

        BookDto request = createBookDto(null, "Заповіт", "978-966-01-0003-5", 1845, 4, 99L);
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookService.create(request));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Автора з id 99 не знайдено");
        verify(bookRepository, never()).save(any());
    }

    // update

    @Test
    void update_existingBookAndAuthor_updatesFieldsAndAuthor() {

        Author shevchenko = createAuthor(1L, "Тарас", "Шевченко");
        Author ukrainka = createAuthor(2L, "Леся", "Українка");
        Book existing = createBook(1L, "Кобзар", "978-966-01-0001-1", 1840, 5, shevchenko);
        BookDto request = createBookDto(null, "Кобзар (повне видання)", "978-966-01-0001-1", 1860, 10, 2L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(2L)).thenReturn(Optional.of(ukrainka));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookDto result = bookService.update(1L, request);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book bookPassedToSave = captor.getValue();
        assertThat(bookPassedToSave).isSameAs(existing);
        assertThat(bookPassedToSave.getId()).isEqualTo(1L);
        assertThat(bookPassedToSave.getTitle()).isEqualTo("Кобзар (повне видання)");
        assertThat(bookPassedToSave.getPublicationYear()).isEqualTo(1860);
        assertThat(bookPassedToSave.getAvailableCopies()).isEqualTo(10);
        assertThat(bookPassedToSave.getAuthor()).isSameAs(ukrainka);

        assertThat(result.getAuthorName()).isEqualTo("Леся Українка");
    }

    @Test
    void update_nonExistingBook_throwsNotFoundAndDoesNotSave() {

        BookDto request = createBookDto(null, "Кобзар", "978-966-01-0001-1", 1840, 5, 1L);
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookService.update(99L, request));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Книгу з id 99 не знайдено");
        verify(authorRepository, never()).findById(anyLong());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_nonExistingAuthor_throwsNotFoundAndLeavesBookUnchanged() {

        Author shevchenko = createAuthor(1L, "Тарас", "Шевченко");
        Book existing = createBook(1L, "Кобзар", "978-966-01-0001-1", 1840, 5, shevchenko);
        BookDto request = createBookDto(null, "Зовсім інша назва", "000", 2000, 0, 99L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookService.update(1L, request));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Автора з id 99 не знайдено");
        assertThat(existing.getTitle()).isEqualTo("Кобзар");
        assertThat(existing.getAvailableCopies()).isEqualTo(5);
        assertThat(existing.getAuthor()).isSameAs(shevchenko);
        verify(bookRepository, never()).save(any());
    }

    // copy

    @Test
    void copy_existingBook_savesNewBookWithSuffixAndSameAuthor() {

        Author shevchenko = createAuthor(1L, "Тарас", "Шевченко");
        Book kobzar = createBook(1L, "Кобзар", "978-966-01-0001-1", 1840, 5, shevchenko);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(kobzar));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(shevchenko));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookDto result = bookService.copy(1L);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book savedCopy = captor.getValue();
        assertThat(savedCopy).isNotSameAs(kobzar);
        assertThat(savedCopy.getId()).isNull();
        assertThat(savedCopy.getTitle()).isEqualTo("Кобзар (копія)");
        assertThat(savedCopy.getIsbn()).isEqualTo("978-966-01-0001-1");
        assertThat(savedCopy.getAuthor()).isSameAs(shevchenko);

        assertThat(result.getTitle()).isEqualTo("Кобзар (копія)");
    }

    @Test
    void copy_nonExistingBook_throwsNotFoundAndDoesNotSave() {

        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookService.copy(99L));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bookRepository, never()).save(any());
    }

    // delete

    @Test
    void delete_existingBook_deletesBook() {

        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.delete(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingBook_throwsNotFoundAndDoesNotDelete() {

        when(bookRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookService.delete(99L));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Книгу з id 99 не знайдено");
        verify(bookRepository, never()).deleteById(anyLong());
    }


    private static Author createAuthor(Long id, String firstName, String lastName) {
        Author author = new Author();
        author.setId(id);
        author.setFirstName(firstName);
        author.setLastName(lastName);
        author.setCountry("Україна");
        return author;
    }

    private static Book createBook(Long id, String title, String isbn, Integer year,
                                   Integer copies, Author author) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPublicationYear(year);
        book.setAvailableCopies(copies);
        book.setAuthor(author);
        return book;
    }

    private static BookDto createBookDto(Long id, String title, String isbn, Integer year,
                                         Integer copies, Long authorId) {
        BookDto dto = new BookDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setIsbn(isbn);
        dto.setPublicationYear(year);
        dto.setAvailableCopies(copies);
        dto.setAuthorId(authorId);
        return dto;
    }
}