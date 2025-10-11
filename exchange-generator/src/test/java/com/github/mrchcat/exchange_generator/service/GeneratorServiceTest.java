package com.github.mrchcat.exchange_generator.service;

import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.List;


@EmbeddedKafka
@DirtiesContext
class GeneratorServiceTest extends AbstractContainerTest{

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${application.kafka.topic.rates}")
    private String TEST_TOPIC;

    @Test
    public void testRatesGenerator() {
        var props = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "rates:com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

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