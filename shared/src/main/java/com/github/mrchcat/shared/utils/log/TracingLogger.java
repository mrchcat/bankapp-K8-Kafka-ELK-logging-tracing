package com.github.mrchcat.shared.utils.log;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TracingLogger {
    private final Tracer tracer;
    private final Logger logger = LogManager.getLogger("KafkaLogger");

    public void debug(String template, Object... args) {
        processLogs(Status.debug, tracer.currentSpan(), template, args);
    }

    public void debug(Span currentSpan, String template, Object... args) {
        processLogs(Status.debug, currentSpan, template, args);
    }

    public void info(String template, Object... args) {
        processLogs(Status.info, tracer.currentSpan(), template, args);
    }

    public void info(Span currentSpan, String template, Object... args) {
        processLogs(Status.info, currentSpan, template, args);
    }

    public void warn(String template, Object... args) {
        processLogs(Status.warn, tracer.currentSpan(), template, args);
    }

    public void warn(Span currentSpan, String template, Object... args) {
        processLogs(Status.warn, currentSpan, template, args);
    }

    public void error(String template, Object... args) {
        processLogs(Status.warn, tracer.currentSpan(), template, args);
    }

    public void error(Span currentSpan, String template, Object... args) {
        processLogs(Status.warn, currentSpan, template, args);
    }

    private void processLogs(Status status, Span currentSpan, String template, Object... args) {
        MDC.put("traceId", (currentSpan != null) ? currentSpan.context().traceId() : "null");
        MDC.put("spanId", (currentSpan != null) ? currentSpan.context().spanId() : "null");
        switch (status) {
            case debug -> logger.debug(template, args);
            case info -> logger.info(template, args);
            case warn -> logger.warn(template, args);
            case error -> logger.error(template, args);
            default -> throw new UnsupportedOperationException();
        }
        MDC.clear();
    }

    private enum Status {
        debug, info, warn, error
    }
}
