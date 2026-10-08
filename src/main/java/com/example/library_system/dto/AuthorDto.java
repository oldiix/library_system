package com.example.library_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorDto {

    private Long id;

    @NotBlank(message = "Ім'я автора обов'язкове")
    private String firstName;

    @NotBlank(message = "Прізвище автора обов'язкове")
    private String lastName;

    private String country;
}