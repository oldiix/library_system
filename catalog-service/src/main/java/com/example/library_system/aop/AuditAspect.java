package com.example.library_system.aop;

import com.example.library_system.audit.AuditEntry;
import com.example.library_system.audit.AuditTrail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final String ANONYMOUS = "anonymous";

    private final AuditTrail auditTrail;
    private final Clock clock;

    @AfterReturning("@annotation(audited)")
    public void recordSuccessfulAction(JoinPoint joinPoint, Audited audited) {
        AuditEntry entry = new AuditEntry(
                Instant.now(clock),
                currentUsername(),
                audited.action(),
                joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()));

        auditTrail.record(entry);
        log.info("AUDIT: користувач '{}' виконав дію {} з аргументами {}",
                entry.username(), entry.action(), entry.arguments());
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : ANONYMOUS;
    }
}