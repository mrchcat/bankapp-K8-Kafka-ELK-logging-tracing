package com.github.mrchcat.transfer.service;

import com.github.mrchcat.shared.accounts.BankUserDto;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.transfer.config.ServiceUrl;
import com.github.mrchcat.transfer.security.OAuthHeaderGetter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Notifications {
    private final String TRANSFER_SERVICE;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topic.notifications}")
    private String notificationTopic;

    public Notifications(KafkaTemplate<String, Object> kafkaTemplate, ServiceUrl serviceUrl) {
        this.TRANSFER_SERVICE = serviceUrl.getTransfer();
        this.kafkaTemplate = kafkaTemplate;
    }


    public void sendNotification(BankUserDto client, String message) throws AuthException {
        var notification = BankNotificationDto.builder()
                .service(TRANSFER_SERVICE)
                .username(client.username())
                .fullName(client.fullName())
                .email(client.email())
                .message(message)
                .build();
        kafkaTemplate.send(notificationTopic, notification);
    }
}
