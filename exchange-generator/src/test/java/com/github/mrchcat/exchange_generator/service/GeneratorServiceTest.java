package com.github.mrchcat.exchange_generator.service;

import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.List;


@EmbeddedKafka
class GeneratorServiceTest extends AbstractContainerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${application.kafka.topic.rates}")
    private String TEST_TOPIC;

    @Test
    public void testRatesGenerator() throws Exception {
        var props = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "rates:com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        try (var consumerForTest = new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(CurrencyExchangeRatesDto.class))
                .createConsumer()) {
            consumerForTest.subscribe(List.of(TEST_TOPIC));
            var record = KafkaTestUtils.getSingleRecord(consumerForTest, TEST_TOPIC, Duration.ofSeconds(5));
            Assertions.assertEquals(2, record.value().exchangeRates().size());
        }
    }
}