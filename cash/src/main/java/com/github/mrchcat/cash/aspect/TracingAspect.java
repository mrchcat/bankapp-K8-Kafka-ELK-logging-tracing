package com.github.mrchcat.cash.aspect;

import com.github.mrchcat.shared.annotation.ToTrace;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TracingAspect {
    private final Tracer tracer;

    @Value("${spring.application.name}")
    String appName;

    @Around("@annotation(toTrace)")
    public Object trace(ProceedingJoinPoint jp, ToTrace toTrace) throws Throwable {
        String spanName = toTrace.spanName() + "-" + appName;
        var newSpan = tracer.nextSpan().name(spanName).start();
        System.out.println("создали спан="+spanName);
        try(Tracer.SpanInScope ws = tracer.withSpan(newSpan.start())) {
            return jp.proceed();
        } finally {
            newSpan.end();
            System.out.println("закрыли спан="+spanName);
        }
    }
}
