package com.example.library_system.dto;

import com.example.library_system.aop.Normalizable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorDto implements Normalizable {

    private Long id;

    @NotBlank(message = "Ім'я автора обов'язкове")
    private String firstName;

    @NotBlank(message = "Прізвище автора обов'язкове")
    private String lastName;

    private String country;

    @Override
    public void normalize() {
        firstName = Normalizable.normalizeText(firstName);
        lastName = Normalizable.normalizeText(lastName);
        country = Normalizable.normalizeOptionalText(country);
    }
}