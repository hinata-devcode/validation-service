package com.venky.validationservice.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Aspect
@Component
public class DeepTracingAspect {

    private static final Logger log = LoggerFactory.getLogger(DeepTracingAspect.class);

    // Targets all your primary functional packages
    @Around("execution(* com.venky.validationservice.controller..*(..)) || " +
            "execution(* com.venky.validationservice.application..*(..)) || " +
            "execution(* com.venky.validationservice.domain..*(..)) || " +
            "execution(* com.venky.validationservice.integration..*(..)) || " +
            "execution(* com.venky.validationservice.persistence.service..*(..))")
    public Object traceEverything(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // LOG INPUT: Method name + all parameters as strings
        log.info("[TRACE-START] {}.{}() | Args: {}", 
            className, 
            methodName, 
            Arrays.deepToString(args));

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            // LOG OUTPUT: Resulting object/data returned
            log.info("[TRACE-END] {}.{}() | Execution: {}ms | Return: {}", 
                className, 
                methodName, 
                executionTime, 
                result);
            
            return result;
        } catch (Exception e) {
            log.error("[TRACE-ERROR] {}.{}() | Exception: {} | Message: {}", 
                className, methodName, e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}