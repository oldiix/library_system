package com.example.library_system.aop;

import com.example.library_system.dto.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class NormalizeInputProxyTest {

    private SampleService proxy;

    @BeforeEach
    void setUp() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new SampleService());
        factory.addAspect(new NormalizeInputAspect());
        proxy = factory.getProxy();
    }

    @Test
    void callThroughProxy_annotatedMethod_receivesNormalizedArgument() {

        BookDto book = bookWithTitle("   Кобзар   ");

        String titleSeenByMethod = proxy.annotatedSave(book);

        assertThat(titleSeenByMethod).isEqualTo("Кобзар");
    }

    @Test
    void callThroughProxy_methodWithoutAnnotation_receivesOriginalArgument() {

        BookDto book = bookWithTitle("   Кобзар   ");

        String titleSeenByMethod = proxy.plainSave(book);

        assertThat(titleSeenByMethod).isEqualTo("   Кобзар   ");
    }

    private static BookDto bookWithTitle(String title) {
        BookDto book = new BookDto();
        book.setTitle(title);
        return book;
    }

    static class SampleService {

        @NormalizeInput
        public String annotatedSave(BookDto book) {
            return book.getTitle();
        }

        public String plainSave(BookDto book) {
            return book.getTitle();
        }
    }
}