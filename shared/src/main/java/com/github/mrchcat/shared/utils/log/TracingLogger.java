package com.github.mrchcat.shared.utils.log;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class TracingLogger {
    private final Tracer tracer;

    public void debug(String template, Object... args) {
        MDC.put("traceId", Objects.requireNonNull(tracer.currentSpan()).context().traceId());
        MDC.put("spanId", Objects.requireNonNull(tracer.currentSpan()).context().spanId());
        log.debug(template, args);
        MDC.clear();
    }

    public void info(String template, Object... args) {
        MDC.put("traceId", Objects.requireNonNull(tracer.currentSpan()).context().traceId());
        MDC.put("spanId", Objects.requireNonNull(tracer.currentSpan()).context().spanId());
        log.info(template, args);
        MDC.clear();
    }

    public void warn(String template, Object... args) {
        MDC.put("traceId", Objects.requireNonNull(tracer.currentSpan()).context().traceId());
        MDC.put("spanId", Objects.requireNonNull(tracer.currentSpan()).context().spanId());
        log.warn(template, args);
        MDC.clear();
    }

    public void error(String template, Object... args) {
        MDC.put("traceId", Objects.requireNonNull(tracer.currentSpan()).context().traceId());
        MDC.put("spanId", Objects.requireNonNull(tracer.currentSpan()).context().spanId());
        log.error(template, args);
        MDC.clear();
    }


}
