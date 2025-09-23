package com.github.mrchcat.notifications.service;

import com.github.mrchcat.notifications.Repository.NotificationRepository;
import com.github.mrchcat.notifications.domain.BankNotification;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.UUID;

@EmbeddedKafka
class NotificationServiceTest extends AbstractContainerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${application.kafka.topic.notifications}")
    private String TEST_TOPIC;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    void testExchangeService() throws InterruptedException {
        embeddedKafkaBroker.addTopicsWithResults(TEST_TOPIC);
        String service= UUID.randomUUID().toString();
        var notificationDto = new BankNotificationDto(service,
                "username",
                "fullname",
                "email",
                "message");
        kafkaTemplate.send(TEST_TOPIC, notificationDto);
        Thread.sleep(3000);
        var notes = notificationRepository.findAll();
        BankNotification notification=notes.iterator().next();
        Assertions.assertTrue(notes.iterator().hasNext());
        Assertions.assertEquals(service,notification.getService());
    }
}