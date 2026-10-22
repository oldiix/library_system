package com.example.library_system.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DtoNormalizationTest {

    @Test
    void authorNormalize_messyNamesAndBlankCountry_cleansNamesAndClearsCountry() {

        AuthorDto author = new AuthorDto();
        author.setFirstName("  Іван ");
        author.setLastName("Франко   ");
        author.setCountry("    ");

        author.normalize();

        assertThat(author.getFirstName()).isEqualTo("Іван");
        assertThat(author.getLastName()).isEqualTo("Франко");
        assertThat(author.getCountry()).isNull();
    }

    @Test
    void bookNormalize_isbnWithSpacesAround_keepsIsbnWithoutSpaces() {

        BookDto book = new BookDto();
        book.setTitle("Кобзар");
        book.setIsbn("  978-966-01-0001-1  ");

        book.normalize();

        assertThat(book.getTitle()).isEqualTo("Кобзар");
        assertThat(book.getIsbn()).isEqualTo("978-966-01-0001-1");
    }

    @Test
    void bookNormalize_missingFields_staysNullWithoutErrors() {

        BookDto book = new BookDto();

        book.normalize();

        assertThat(book.getTitle()).isNull();
        assertThat(book.getIsbn()).isNull();
    }
}