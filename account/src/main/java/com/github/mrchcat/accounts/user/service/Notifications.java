package com.github.mrchcat.accounts.user.service;

import com.github.mrchcat.accounts.config.ServiceUrl;
import com.github.mrchcat.accounts.security.OAuthHeaderGetter;
import com.github.mrchcat.accounts.user.model.BankUser;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.security.auth.message.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Notifications {
    private final String ACCOUNT_SERVICE;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.kafka.topic.notifications}")
    private String notificationTopic;

    public Notifications(ServiceUrl serviceUrl, KafkaTemplate<String, Object> kafkaTemplate) {
        this.ACCOUNT_SERVICE = serviceUrl.getAccount();
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(BankUser client, String message) throws AuthException {
        var notification = BankNotificationDto.builder()
                .service(ACCOUNT_SERVICE)
                .username(client.getUsername())
                .fullName(client.getFullName())
                .email(client.getEmail())
                .message(message)
                .build();
        kafkaTemplate.send(notificationTopic, notification);
    }
}
