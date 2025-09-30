package com.github.mrchcat.accounts.config;

import com.github.mrchcat.shared.utils.trace.TracingAspect;
import io.micrometer.tracing.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AOPConfig {

    @Bean
    TracingAspect getTracingAspect(Tracer tracer) {
        return new TracingAspect(tracer);
    }
}
