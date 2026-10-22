package com.example.library_system.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class NormalizeInputAspect {

    @Before("@annotation(com.example.library_system.aop.NormalizeInput)")
    public void normalizeArguments(JoinPoint joinPoint) {
        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Normalizable normalizable) {
                normalizable.normalize();
                log.debug("Нормалізовано аргумент типу {}", argument.getClass().getSimpleName());
            }
        }
    }
}