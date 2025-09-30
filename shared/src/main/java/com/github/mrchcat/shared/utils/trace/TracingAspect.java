package com.github.mrchcat.shared.utils.trace;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class TracingAspect {

    private final Tracer tracer;

    @Around("@annotation(toTrace)")
    public Object trace(ProceedingJoinPoint jp, ToTrace toTrace) throws Throwable {
        String spanName = toTrace.spanName();
        System.out.println("спан=" + spanName + "теги=" + Arrays.toString(toTrace.tags()));
        var newSpan = tracer.nextSpan().name(spanName).start();
        String[] tags = toTrace.tags();
        for (String pair : tags) {
            if (!pair.isBlank() && pair.matches("\\w+:\\w+")) {
                String tag[] = pair.split(":");
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
