package com.example.library_system.aop;

import com.example.library_system.dto.BookDto;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NormalizeInputAspectTest {

    private final NormalizeInputAspect aspect = new NormalizeInputAspect();

    @Mock
    private JoinPoint joinPoint;

    @Test
    void normalizeArguments_normalizableArgument_cleansItsFields() {

        BookDto book = new BookDto();
        book.setTitle("   Лісова    пісня  ");
        book.setIsbn("   ");
        when(joinPoint.getArgs()).thenReturn(new Object[]{5L, book});

        aspect.normalizeArguments(joinPoint);

        assertThat(book.getTitle()).isEqualTo("Лісова пісня");
        assertThat(book.getIsbn()).isNull();
    }

    @Test
    void normalizeArguments_noNormalizableArguments_leavesThemUntouched() {

        Object[] arguments = {5L, "  text  "};
        when(joinPoint.getArgs()).thenReturn(arguments);

        assertDoesNotThrow(() -> aspect.normalizeArguments(joinPoint));
        assertThat(arguments).containsExactly(5L, "  text  ");
    }
}