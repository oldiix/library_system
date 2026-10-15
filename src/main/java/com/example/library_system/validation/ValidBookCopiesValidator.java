package com.example.library_system.validation;

import com.example.library_system.dto.BookDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidBookCopiesValidator implements ConstraintValidator<ValidBookCopies, BookDto> {

    private int maxCopiesWithoutIsbn;

    @Override
    public void initialize(ValidBookCopies annotation) {
        this.maxCopiesWithoutIsbn = annotation.maxCopiesWithoutIsbn();
    }

    @Override
    public boolean isValid(BookDto book, ConstraintValidatorContext context) {

        if (book == null || book.getAvailableCopies() == null) {
            return true;
        }

        boolean hasIsbn = book.getIsbn() != null && !book.getIsbn().isBlank();
        if (hasIsbn || book.getAvailableCopies() <= maxCopiesWithoutIsbn) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("availableCopies")
                .addConstraintViolation();
        return false;
    }
}