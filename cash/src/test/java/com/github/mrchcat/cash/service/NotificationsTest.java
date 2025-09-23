package com.github.mrchcat.cash.service;

import com.github.mrchcat.shared.accounts.BankUserDto;
import com.github.mrchcat.shared.notification.BankNotificationDto;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@EmbeddedKafka
class NotificationsTest extends AbstractContainerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${application.kafka.topic.notifications}")
    private String TEST_TOPIC;

    @Autowired
    Notifications notificationService;


    @Test
    public void testRatesGenerator() throws Exception {
        String message = UUID.randomUUID().toString();
        var bankUserDto = new BankUserDto(UUID.randomUUID(),
                "name", "name",
                LocalDate.of(2000, 1, 1),
                "email"
                , Collections.emptyList());
        notificationService.sendNotification(bankUserDto, message);
        Thread.sleep(3000);
        var props = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        props.put(JsonDeserializer.TYPE_MAPPINGS, "note:com.github.mrchcat.shared.notification.BankNotificationDto");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        try (var consumerForTest = new DefaultKafkaConsumerFactory<>(props,
                new StringDeserializer(),
                new JsonDeserializer<>(BankNotificationDto.class))
                .createConsumer()) {
            consumerForTest.subscribe(List.of(TEST_TOPIC));
            var record = KafkaTestUtils.getSingleRecord(consumerForTest, TEST_TOPIC, Duration.ofSeconds(5));
            Assertions.assertEquals(message, record.value().message());
        }
    }

}