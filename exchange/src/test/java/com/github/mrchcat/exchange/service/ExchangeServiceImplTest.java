package com.github.mrchcat.exchange.service;

import com.github.mrchcat.shared.enums.BankCurrency;
import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import com.github.mrchcat.shared.exchange.CurrencyRate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@EmbeddedKafka
class ExchangeServiceImplTest extends AbstractContainerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${application.kafka.topic.rates}")
    private String TEST_TOPIC;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    ExchangeService exchangeService;

    @Test
    void testExchangeService() throws InterruptedException {
        embeddedKafkaBroker.addTopicsWithResults(TEST_TOPIC);
        var USDrate = BigDecimal.valueOf(100);
        var CNYrate = BigDecimal.valueOf(18);
        var sellCoefficient = BigDecimal.valueOf(1.2);
        var rates = getCurrencyExchangeRatesDto(USDrate, CNYrate, sellCoefficient);
        kafkaTemplate.send(TEST_TOPIC, rates);
        Thread.sleep(3000);
        var USDrateFromService = exchangeService.getExchangeRate(BankCurrency.USD, BankCurrency.RUB).getRate();
        var CNYrateFromService = exchangeService.getExchangeRate(BankCurrency.CNY, BankCurrency.RUB).getRate();
        Assertions.assertEquals(USDrate.multiply(sellCoefficient), USDrateFromService);
        Assertions.assertEquals(CNYrate.multiply(sellCoefficient), CNYrateFromService);
    }

    private CurrencyExchangeRatesDto getCurrencyExchangeRatesDto(BigDecimal USDrate,
                                                                 BigDecimal CNYrate,
                                                                 BigDecimal sellCoefficient) {
        var USDRate = CurrencyRate.builder()
                .currency(BankCurrency.USD)
                .buyRate(USDrate)
                .sellRate(USDrate.multiply(sellCoefficient))
                .time(LocalDateTime.now())
                .build();
        var CNYRate = CurrencyRate.builder()
                .currency(BankCurrency.CNY)
                .buyRate(CNYrate)
                .sellRate(CNYrate.multiply(sellCoefficient))
                .time(LocalDateTime.now())
                .build();
        return new CurrencyExchangeRatesDto(BankCurrency.RUB, List.of(USDRate, CNYRate));
    }
}