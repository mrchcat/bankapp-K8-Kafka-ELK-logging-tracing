package com.github.mrchcat.exchange.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeServiceImplTest extends AbstractContainerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${application.kafka.topic.rates}")
    private String TEST_TOPIC;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void testExchangeService(){

    }

}