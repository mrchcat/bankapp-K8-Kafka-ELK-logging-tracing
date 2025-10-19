package com.github.mrchcat.shared.utils.trace;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TracingAspect {

    private final Tracer tracer;

    @Around("@annotation(toTrace)")
    public Object trace(ProceedingJoinPoint jp, ToTrace toTrace) throws Throwable {
        String spanName = toTrace.spanName();
        if (spanName == null || spanName.isBlank()) {
            throw new NullPointerException("span id empty");
        }
        var newSpan = tracer.nextSpan().name(spanName).start();
        for (String pair : toTrace.tags()) {
            if (pair != null && !pair.isBlank() && pair.matches("\\w+:\\w+")) {
                String[] tag = pair.split(":");
                newSpan.tag(tag[0], tag[1]);
            }
        }
        try (Tracer.SpanInScope ws = tracer.withSpan(newSpan.start())) {
            return jp.proceed();
        } finally {
            newSpan.end();
        }
    }

}
