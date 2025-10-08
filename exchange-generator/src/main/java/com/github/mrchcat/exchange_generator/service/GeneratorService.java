package com.github.mrchcat.exchange_generator.service;

import com.github.mrchcat.shared.enums.BankCurrency;
import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import com.github.mrchcat.shared.exchange.CurrencyRate;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import com.github.mrchcat.shared.utils.trace.ToTrace;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeneratorService {

    private final BigDecimal AVERAGE_USD = BigDecimal.valueOf(80);
    private final BigDecimal AVERAGE_CNY = BigDecimal.valueOf(11);

    @Value("${application.kafka.topic.rates}")
    private String ratesTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;
    private final TracingLogger tracingLogger;

    @Scheduled(fixedDelay = 1000L)
    @ToTrace(spanName = "kafka", tags = {"operation:send_exchange_rates"})
    public void sendNewRates() {
        var rates = new CurrencyExchangeRatesDto(BankCurrency.RUB, getRates());
        var currentSpan = tracer.currentSpan();
        kafkaTemplate
                .send(ratesTopic, 0, "rates", rates)
                .whenComplete((result, e) -> {
                            if (e == null) {
                                tracingLogger.debug(currentSpan, "Курсы валют отправлены в брокер: {}", rates);
                            } else {
                                tracingLogger.error(currentSpan, "Ошибка при отправке курсов валют в брокер сообщений: {} ", e.getMessage());
                            }
                        }
                );
    }

    private List<CurrencyRate> getRates() {
        BigDecimal randomBuyUsd = BigDecimal.valueOf(1 - Math.random() / 10);
        BigDecimal randomSellUsd = BigDecimal.valueOf(1 + Math.random() / 10);

        BigDecimal randomBuyCNY = BigDecimal.valueOf(1 - Math.random() / 10);
        BigDecimal randomSellCNY = BigDecimal.valueOf(1 + Math.random() / 10);

        var USDRate = CurrencyRate.builder()
                .currency(BankCurrency.USD)
                .buyRate(AVERAGE_USD.multiply(randomBuyUsd).setScale(2, RoundingMode.HALF_UP))
                .sellRate(AVERAGE_USD.multiply(randomSellUsd).setScale(2, RoundingMode.HALF_UP))
                .time(LocalDateTime.now())
                .build();
        var CNYRate = CurrencyRate.builder()
                .currency(BankCurrency.CNY)
                .buyRate(AVERAGE_CNY.multiply(randomBuyCNY).setScale(2, RoundingMode.HALF_UP))
                .sellRate(AVERAGE_CNY.multiply(randomSellCNY).setScale(2, RoundingMode.HALF_UP))
                .time(LocalDateTime.now())
                .build();
        return List.of(USDRate, CNYRate);
    }
}
