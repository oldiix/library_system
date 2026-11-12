package com.example.library_system.aop;

import com.example.library_system.audit.AuditEntry;
import com.example.library_system.audit.AuditTrail;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    private static final Instant FIXED_TIME = Instant.parse("2026-10-22T10:15:30Z");

    @Mock
    private AuditTrail auditTrail;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    private AuditAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new AuditAspect(auditTrail, Clock.fixed(FIXED_TIME, ZoneOffset.UTC));
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("BookService.delete(..)");
        when(joinPoint.getArgs()).thenReturn(new Object[]{5L});
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void recordSuccessfulAction_authenticatedUser_recordsUserActionMethodAndArguments() throws Exception {

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("alice", null, List.of()));

        aspect.recordSuccessfulAction(joinPoint, auditedOf("deleteBook"));

        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditTrail).record(captor.capture());
        AuditEntry entry = captor.getValue();
        assertThat(entry.timestamp()).isEqualTo(FIXED_TIME);
        assertThat(entry.username()).isEqualTo("alice");
        assertThat(entry.action()).isEqualTo("BOOK_DELETED");
        assertThat(entry.method()).isEqualTo("BookService.delete(..)");
        assertThat(entry.arguments()).isEqualTo("[5]");
    }

    @Test
    void recordSuccessfulAction_noAuthenticatedUser_recordsAnonymous() throws Exception {

        aspect.recordSuccessfulAction(joinPoint, auditedOf("deleteBook"));

        ArgumentCaptor<AuditEntry> captor = ArgumentCaptor.forClass(AuditEntry.class);
        verify(auditTrail).record(captor.capture());
        assertThat(captor.getValue().username()).isEqualTo("anonymous");
    }

    private static Audited auditedOf(String methodName) throws NoSuchMethodException {
        return AnnotatedMethods.class.getDeclaredMethod(methodName).getAnnotation(Audited.class);
    }

    @SuppressWarnings("unused")
    static class AnnotatedMethods {

        @Audited(action = "BOOK_DELETED")
        void deleteBook() {
        }
    }
}