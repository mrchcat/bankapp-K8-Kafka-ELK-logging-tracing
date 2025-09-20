package com.github.mrchcat.exchange.config;

import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.AfterRollbackProcessor;
import org.springframework.kafka.listener.DefaultAfterRollbackProcessor;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConfig {
    @Bean
    public AfterRollbackProcessor<String, CurrencyExchangeRatesDto> afterRollbackProcessor() {
        return new DefaultAfterRollbackProcessor<>((record, exception) -> {
        }, new FixedBackOff(100L, 5L));
    }
}
