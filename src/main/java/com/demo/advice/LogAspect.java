package com.demo.advice;

import com.demo.pojo.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Around("@annotation(bizLog)")
    public Object around(ProceedingJoinPoint joinPoint, BizLog bizLog) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Long userId = UserContext.get();
        log.info("userId = {}, method name = {}, desc = {}", userId, methodName, bizLog.value());
        Object[] args = joinPoint.getArgs();
        try {
            Object result = joinPoint.proceed(args);
            long end = System.currentTimeMillis();

            log.info("[BIZ_OK]userId = {}, method name = {}, desc = {}, cost = {}", userId, methodName, bizLog.value(), end - start);

            return result;
        } catch (Throwable e) {
            long end = System.currentTimeMillis();
            log.error("[BIZ_ERR] useId = {}, method name = {}, desc = {}, cost = {}ms", userId, methodName, bizLog.value(), end - start);
            log.error(e.toString());
            throw e;
        }


    }
}
