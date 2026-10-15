package com.example.library_system.validation;

import com.example.library_system.dto.BookDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidBookCopiesTest {

    private static final String EXPECTED_MESSAGE =
            "Книга без ISBN вважається унікальним екземпляром (рукопис, стародрук), "
                    + "тому кількість примірників не може перевищувати 1";

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void bookDto_annotatedWithValidBookCopies_validatorIsRegisteredThroughConstraintAnnotation() {

        Set<ConstraintDescriptor<?>> classConstraints =
                validator.getConstraintsForClass(BookDto.class).getConstraintDescriptors();

        List<ConstraintDescriptor<?>> validBookCopiesConstraints = classConstraints.stream()
                .filter(descriptor -> descriptor.getAnnotation() instanceof ValidBookCopies)
                .toList();
        assertThat(validBookCopiesConstraints).hasSize(1);

        List<Class<?>> validatorClasses =
                List.copyOf(validBookCopiesConstraints.getFirst().getConstraintValidatorClasses());
        assertThat(validatorClasses).containsExactly(ValidBookCopiesValidator.class);
    }


    @Test
    void validate_bookWithIsbnAndManyCopies_noViolations() {

        BookDto book = createBook("978-966-01-0001-1", 10);

        Set<ConstraintViolation<BookDto>> violations = validator.validate(book);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_bookWithoutIsbnAndExactlyOneCopy_noViolations() {

        BookDto book = createBook(null, 1);

        Set<ConstraintViolation<BookDto>> violations = validator.validate(book);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_bookWithoutIsbnAndZeroCopies_noViolations() {

        BookDto book = createBook(null, 0);

        Set<ConstraintViolation<BookDto>> violations = validator.validate(book);

        assertThat(violations).isEmpty();
    }


    @Test
    void validate_bookWithoutIsbnAndTwoCopies_returnsViolationWithClearMessage() {

        BookDto book = createBook(null, 2);

        Set<ConstraintViolation<BookDto>> violations = validator.validate(book);

        assertThat(violations).hasSize(1);
        ConstraintViolation<BookDto> violation = violations.iterator().next();
        assertThat(violation.getMessage()).isEqualTo(EXPECTED_MESSAGE);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("availableCopies");
        assertThat(violation.getConstraintDescriptor().getAnnotation()).isInstanceOf(ValidBookCopies.class);
    }

    @Test
    void validate_bookWithBlankIsbnAndThreeCopies_treatsBlankIsbnAsMissing() {

        BookDto book = createBook("   ", 3);

        Set<ConstraintViolation<BookDto>> violations = validator.validate(book);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo(EXPECTED_MESSAGE);
    }

    @Test
    void validate_bookWithoutCopiesCount_reportsOnlyNotNullViolation() {

        BookDto book = createBook(null, null);

        Set<ConstraintViolation<BookDto>> violations = validator.validate(book);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Кількість примірників обов'язкова");
    }

    private static BookDto createBook(String isbn, Integer availableCopies) {
        BookDto book = new BookDto();
        book.setTitle("Тестова книга");
        book.setAuthorId(1L);
        book.setIsbn(isbn);
        book.setAvailableCopies(availableCopies);
        return book;
    }
}