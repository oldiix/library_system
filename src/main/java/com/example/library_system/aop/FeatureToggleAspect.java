package com.example.library_system.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {

    private final FeatureProperties featureProperties;

    @Around("@annotation(featureToggle)")
    public Object checkFeatureEnabled(ProceedingJoinPoint joinPoint, FeatureToggle featureToggle) throws Throwable {
        String feature = featureToggle.value();

        if (!featureProperties.isEnabled(feature)) {
            log.warn("Виклик заблоковано: функцію '{}' вимкнено в налаштуваннях", feature);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Функцію '" + feature + "' тимчасово вимкнено адміністратором");
        }

        return joinPoint.proceed();
    }
}