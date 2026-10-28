package com.example.library_system.controller;

import com.example.library_system.dto.BookDetailsResponse;
import com.example.library_system.dto.BookDto;
import com.example.library_system.external.BookRating;
import com.example.library_system.external.StoreOffer;
import com.example.library_system.service.BookDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookDetailsControllerTest {

    @Mock
    private BookDetailsService bookDetailsService;

    @Test
    void getDetails_existingBook_returnsBookRatingAndOfferAsJson() throws Exception {

        BookDto book = new BookDto();
        book.setId(1L);
        book.setTitle("Kobzar");
        when(bookDetailsService.getDetails(1L)).thenReturn(new BookDetailsResponse(
                book, new BookRating(4.8, 120, true), StoreOffer.unavailable()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new BookDetailsController(bookDetailsService)).build();

        ResultActions result = mockMvc.perform(get("/api/books/1/details"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.book.title").value("Kobzar"))
                .andExpect(jsonPath("$.rating.average").value(4.8))
                .andExpect(jsonPath("$.rating.available").value(true))
                .andExpect(jsonPath("$.offer.available").value(false));
    }
}