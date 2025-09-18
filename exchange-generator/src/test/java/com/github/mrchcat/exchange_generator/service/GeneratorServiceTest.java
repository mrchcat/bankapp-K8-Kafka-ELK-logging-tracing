package com.github.mrchcat.exchange_generator.service;

import com.github.mrchcat.shared.enums.BankCurrency;
import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import com.github.mrchcat.shared.exchange.CurrencyRate;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.kafka.test.hamcrest.KafkaMatchers.hasValue;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("test")
//@EmbeddedKafka(topics = {"bank-exchange-rates"}, partitions = 1)
class GeneratorServiceTest {
//    @Autowired
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    @Autowired
//    private EmbeddedKafkaBroker embeddedKafkaBroker;
//
//    private final String ratesTopic = "bank-exchange-rates";
//
//    @Test
//    public void testSimpleProcessor() {
//        var rates =getCurrencyExchangeRatesDto();
//        kafkaTemplate.send(ratesTopic, rates);
//        try (var consumer = new DefaultKafkaConsumerFactory<>(
//                KafkaTestUtils.consumerProps("test", "true", embeddedKafkaBroker),
//                new StringDeserializer(), new JsonDeserializer<>(CurrencyExchangeRatesDto.class)
//        ).createConsumer()) {
//            consumer.subscribe(List.of(ratesTopic));
//            var record = KafkaTestUtils.getSingleRecord(consumer, ratesTopic, Duration.ofSeconds(5));
//            MatcherAssert.assertThat(record,hasValue(rates));
//        }
//    }
//
//    private CurrencyExchangeRatesDto getCurrencyExchangeRatesDto() {
//        var USDrate = CurrencyRate.builder()
//                .currency(BankCurrency.USD)
//                .buyRate(BigDecimal.valueOf(100))
//                .sellRate(BigDecimal.valueOf(110))
//                .time(LocalDateTime.now())
//                .build();
//        var CNYrate = CurrencyRate.builder()
//                .currency(BankCurrency.CNY)
//                .buyRate(BigDecimal.valueOf(20))
//                .sellRate(BigDecimal.valueOf(22))
//                .time(LocalDateTime.now())
//                .build();
//        return new CurrencyExchangeRatesDto(BankCurrency.RUB, List.of(USDrate, CNYrate));
//    }
}