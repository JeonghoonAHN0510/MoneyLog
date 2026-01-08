package com.moneylog_backend.global.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.moneylog_backend.global.log.dto.LogDto;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class MethodLoggingAspect {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Around("execution(* com.moneylog_backend.moneylog..*Service.*(..))")
    public Object logExecution (ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();
        String traceId = MDC.get("traceId");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = null;
        boolean isSuccess = true;
        String resultString = "";

        try {
            result = joinPoint.proceed();
            resultString = result != null ? result.toString() : "void";
            return result;
        } catch (Exception e) {
            isSuccess = false;
            resultString = e.getMessage();
            throw e;
        } finally {
            stopWatch.stop();

            applicationEventPublisher.publishEvent(
                new LogDto(traceId, className, methodName, Arrays.toString(args), resultString,
                           stopWatch.getTotalTimeMillis(), isSuccess));
        }
    }
}
