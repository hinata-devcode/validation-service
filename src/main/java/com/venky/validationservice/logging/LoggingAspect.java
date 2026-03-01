package com.venky.validationservice.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // 1. Target the Controller package
    @Pointcut("execution(* com.venky.validationservice.controller..*(..))")
    public void controllerLayer() {}

    // 2. Target the Application layer
    @Pointcut("execution(* com.venky.validationservice.application..*(..))")
    public void applicationLayer() {}

    // 3. Target the Domain Service layer
    @Pointcut("execution(* com.venky.validationservice.domain.service..*(..))")
    public void domainLayer() {}

    // 4. Target the Integration layer (This automatically includes Razorpay and Webhooks)
    @Pointcut("execution(* com.venky.validationservice.integration..*(..))")
    public void integrationLayer() {}

    // 5. Combine them all into one master rule
    @Pointcut("controllerLayer() || applicationLayer() || domainLayer() || integrationLayer()")
    public void allMainLayers() {}

    /**
     * This method intercepts only the layers defined above.
     * It completely ignores DTOs, Entities, and Configuration files!
     */
    @Around("allMainLayers()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.debug("STARTED: {}.{}()", className, methodName);
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("FINISHED: {}.{}() in {} ms", className, methodName, executionTime);
            
            return result;
            
        } catch (IllegalArgumentException e) {
            log.error("ILLEGAL ARGUMENT in {}.{}(): {}", className, methodName, e.getMessage());
            throw e;
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("FAILED: {}.{}() after {} ms. Error: {}", className, methodName, executionTime, e.getMessage());
            throw e; // Rethrow to let your GlobalExceptionHandler catch it
        }
    }
}