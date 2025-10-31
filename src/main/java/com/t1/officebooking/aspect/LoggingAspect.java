package com.t1.officebooking.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Aspect
public class LoggingAspect {
    @Pointcut("execution(* com.t1.officebooking.authorization.service.*.*(..)) || " +
            "execution(* com.t1.officebooking.service.*.*(..))")
    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void logBeforeService(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("{} Method is about to execute with params: {}", methodName, joinPoint.getArgs());
    }

    @AfterReturning(
            value = "serviceMethods()",
            returning = "result"
    )
    public void logAfterService(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("{} Method executed successfully with result: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "execution(* com.t1.officebooking.authorization.controller.*.*(..)) || "
            + "execution(* com.t1.officebooking.controller.*.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        log.error("Exception occurred in controller method: {}", joinPoint.getSignature().getName());
        log.error("{} Exception message: {}", ex.getClass(), ex.getMessage());
    }

}
