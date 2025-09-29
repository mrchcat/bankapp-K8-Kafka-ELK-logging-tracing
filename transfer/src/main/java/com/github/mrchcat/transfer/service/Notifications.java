package com.github.mrchcat.transfer.service;

import com.github.mrchcat.shared.accounts.BankUserDto;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.transfer.config.ServiceUrl;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Notifications {
    private final String TRANSFER_SERVICE;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;

    @Value("${application.kafka.topic.notifications}")
    private String notificationTopic;

    public Notifications(KafkaTemplate<String, Object> kafkaTemplate, ServiceUrl serviceUrl,Tracer tracer) {
        this.TRANSFER_SERVICE = serviceUrl.getTransfer();
        this.kafkaTemplate = kafkaTemplate;
        this.tracer=tracer;
    }


    public void sendNotification(BankUserDto client, String message){
        var notification = BankNotificationDto.builder()
                .service(TRANSFER_SERVICE)
                .username(client.username())
                .fullName(client.fullName())
                .email(client.email())
                .message(message)
                .build();
        var kafkaSpan = tracer.nextSpan().name("bank-kafka-" + notificationTopic).start();
        try{
            kafkaTemplate.send(notificationTopic, notification);
        } finally {
            kafkaSpan.end();
        }
    }
}
