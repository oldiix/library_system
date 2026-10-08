package com.example.library_system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookDto {

    private Long id;

    @NotBlank(message = "Назва книги обов'язкова")
    private String title;

    private String isbn;

    private Integer publicationYear;

    @NotNull(message = "Кількість примірників обов'язкова")
    @Min(value = 0, message = "Кількість примірників не може бути від'ємною")
    private Integer availableCopies;

    @NotNull(message = "Потрібно вказати id автора")
    private Long authorId;

    private String authorName;
}