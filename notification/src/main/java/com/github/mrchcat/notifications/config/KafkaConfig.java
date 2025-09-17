package com.github.mrchcat.notifications.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.shaded.com.google.protobuf.Any;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.AfterRollbackProcessor;
import org.springframework.kafka.listener.DefaultAfterRollbackProcessor;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConfig {
    @Bean
    public AfterRollbackProcessor<Any, Any> afterRollbackProcessor() {
        return new DefaultAfterRollbackProcessor<Any, Any>((record, exception) -> {
        }, new FixedBackOff(100L, 5L));
    }
}
