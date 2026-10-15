package com.example.library_system.controller;

import com.example.library_system.dto.AuthorDto;
import com.example.library_system.dto.BookDto;
import com.example.library_system.service.AuthorService;
import com.example.library_system.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CreateEndpointsTest {

    @Mock
    private AuthorService authorService;

    @Mock
    private BookService bookService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthorController(authorService), new BookController(bookService))
                .build();
    }

    @Test
    void createAuthor_validRequest_returns201AndCreatedAuthor() throws Exception {

        AuthorDto created = new AuthorDto();
        created.setId(3L);
        created.setFirstName("Ivan");
        created.setLastName("Franko");
        created.setCountry("Ukraine");
        when(authorService.create(any(AuthorDto.class))).thenReturn(created);

        ResultActions result = mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName": "Ivan", "lastName": "Franko", "country": "Ukraine"}
                        """));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.lastName").value("Franko"));
        verify(authorService).create(any(AuthorDto.class));
    }

    @Test
    void createBook_validRequest_returns201AndPassesRequestToService() throws Exception {

        BookDto created = new BookDto();
        created.setId(10L);
        created.setTitle("Zakhar Berkut");
        created.setAvailableCopies(4);
        created.setAuthorId(3L);
        when(bookService.create(any(BookDto.class))).thenReturn(created);

        ResultActions result = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Zakhar Berkut", "isbn": "978-966-01-0005-9",
                         "publicationYear": 1883, "availableCopies": 4, "authorId": 3}
                        """));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Zakhar Berkut"));

        ArgumentCaptor<BookDto> captor = ArgumentCaptor.forClass(BookDto.class);
        verify(bookService).create(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Zakhar Berkut");
        assertThat(captor.getValue().getAuthorId()).isEqualTo(3L);
    }

    @Test
    void createBook_missingTitle_returns400AndDoesNotCallService() throws Exception {

        ResultActions result = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"availableCopies": 1, "authorId": 1}
                        """));

        result.andExpect(status().isBadRequest());
        verify(bookService, never()).create(any());
    }

    @Test
    void createBook_withoutIsbnAndThreeCopies_returns400ByCustomConstraint() throws Exception {

        ResultActions result = mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Manuscript", "availableCopies": 3, "authorId": 1}
                        """));

        result.andExpect(status().isBadRequest());
        verify(bookService, never()).create(any());
    }
}