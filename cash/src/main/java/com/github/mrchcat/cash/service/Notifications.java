package com.github.mrchcat.cash.service;

import com.github.mrchcat.cash.config.ServiceUrl;
import com.github.mrchcat.shared.accounts.BankUserDto;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.shared.utils.trace.ToTrace;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Notifications {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String CASH_SERVICE;

    @Value("${application.kafka.topic.notifications}")
    private String notificationTopic;
    private final Tracer tracer;

    public Notifications(KafkaTemplate<String, Object> kafkaTemplate, ServiceUrl serviceUrl, Tracer tracer) {
        this.kafkaTemplate = kafkaTemplate;
        this.CASH_SERVICE = serviceUrl.getCash();
        this.tracer = tracer;
    }

    @ToTrace(spanName = "kafka", tags = {"operation:send_notification"})
    public void sendNotification(BankUserDto client, String message) {
        var notification = BankNotificationDto.builder()
                .service(CASH_SERVICE)
                .username(client.username())
                .fullName(client.fullName())
                .email(client.email())
                .message(message)
                .build();
        kafkaTemplate.send(notificationTopic, notification);
    }
}
