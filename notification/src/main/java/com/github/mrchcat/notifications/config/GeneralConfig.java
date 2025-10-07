package com.github.mrchcat.notifications.config;

import com.github.mrchcat.shared.utils.log.TracingLogger;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralConfig {

    @Bean
    TracingLogger getTracingLogger(Tracer tracer) {
        return new TracingLogger(tracer);
    }
}
