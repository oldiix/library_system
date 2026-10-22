package com.example.library_system.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    private final FeatureToggleAspect aspect = new FeatureToggleAspect(
            new FeatureProperties(Map.of(
                    "book-deletion", true,
                    "author-deletion", false)));

    @Test
    void checkFeatureEnabled_featureEnabled_proceedsAndReturnsMethodResult() throws Throwable {

        when(joinPoint.proceed()).thenReturn("method result");

        Object result = aspect.checkFeatureEnabled(joinPoint, toggleOf("bookDeletion"));

        verify(joinPoint, times(1)).proceed();
        assertThat(result).isEqualTo("method result");
    }

    @Test
    void checkFeatureEnabled_featureDisabled_throwsServiceUnavailableWithoutProceeding() throws Throwable {

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> aspect.checkFeatureEnabled(joinPoint, toggleOf("authorDeletion")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(exception.getReason()).isEqualTo("Функцію 'author-deletion' тимчасово вимкнено адміністратором");
        verify(joinPoint, never()).proceed();
    }

    @Test
    void checkFeatureEnabled_featureMissingInSettings_treatedAsDisabled() throws Throwable {

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> aspect.checkFeatureEnabled(joinPoint, toggleOf("unknownFeature")));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        verify(joinPoint, never()).proceed();
    }

    @Test
    void checkFeatureEnabled_featureEnabledButMethodFails_rethrowsOriginalException() throws Throwable {

        ResponseStatusException notFound = new ResponseStatusException(HttpStatus.NOT_FOUND, "Книгу не знайдено");
        when(joinPoint.proceed()).thenThrow(notFound);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> aspect.checkFeatureEnabled(joinPoint, toggleOf("bookDeletion")));

        assertThat(exception).isSameAs(notFound);
        verify(joinPoint, times(1)).proceed();
    }

    private static FeatureToggle toggleOf(String methodName) throws NoSuchMethodException {
        return AnnotatedMethods.class.getDeclaredMethod(methodName).getAnnotation(FeatureToggle.class);
    }

    @SuppressWarnings("unused")
    static class AnnotatedMethods {

        @FeatureToggle("book-deletion")
        void bookDeletion() {
        }

        @FeatureToggle("author-deletion")
        void authorDeletion() {
        }

        @FeatureToggle("unknown-feature")
        void unknownFeature() {
        }
    }
}