package com.example.library_system.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidBookCopiesValidator.class)
public @interface ValidBookCopies {

    String message() default "Книга без ISBN вважається унікальним екземпляром (рукопис, стародрук), "
            + "тому кількість примірників не може перевищувати {maxCopiesWithoutIsbn}";

    int maxCopiesWithoutIsbn() default 1;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}