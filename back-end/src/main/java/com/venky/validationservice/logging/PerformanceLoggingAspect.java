package com.venky.validationservice.logging;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    // Targets your main layers. (Excluding Schedulers to avoid constant background noise)
    @Around("execution(* com.venky.validationservice.controller..*(..)) || " +
            "execution(* com.venky.validationservice.application..*(..)) || " +
            "execution(* com.venky.validationservice.integration..*(..)) || " +
            "execution(* com.venky.validationservice.persistence.service..*(..))")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        
        long start = System.currentTimeMillis();
        
        // Execute the actual method
        Object result = joinPoint.proceed(); 
        
        long executionTime = System.currentTimeMillis() - start;
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        if (executionTime > 500) {
            // If it takes more than half a second, log it as a WARNING so we notice it!
            log.warn(" SLOW EXECUTION: {}.{}() took {}ms", className, methodName, executionTime);
        } else {
            // Otherwise, log as DEBUG. In production (where level is INFO), this is silently ignored!
            log.debug("Execution time: {}.{}() took {}ms", className, methodName, executionTime);
        }

        return result;
    }
}
