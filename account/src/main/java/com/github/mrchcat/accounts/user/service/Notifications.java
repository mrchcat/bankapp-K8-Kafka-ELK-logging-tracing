package com.github.mrchcat.accounts.user.service;

import com.github.mrchcat.accounts.config.ServiceUrl;
import com.github.mrchcat.accounts.user.model.BankUser;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.shared.utils.trace.ToTrace;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Notifications {
    private final String ACCOUNT_SERVICE;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;

    @Value("${application.kafka.topic.notifications}")
    private String notificationTopic;

    public Notifications(ServiceUrl serviceUrl, KafkaTemplate<String, Object> kafkaTemplate, Tracer tracer) {
        this.ACCOUNT_SERVICE = serviceUrl.getAccount();
        this.kafkaTemplate = kafkaTemplate;
        this.tracer = tracer;
    }

    @ToTrace(spanName = "kafka", tags = {"operation:send_notification"})
    public void sendNotification(BankUser client, String message) {
        var notification = BankNotificationDto.builder()
                .service(ACCOUNT_SERVICE)
                .username(client.getUsername())
                .fullName(client.getFullName())
                .email(client.getEmail())
                .message(message)
                .build();
        kafkaTemplate
                .send(notificationTopic, notification)
                .whenComplete((result, e) -> {
                            if (e != null) {
                                log.error("Ошибка при отправке сообщения: {} ", e.getMessage());
                            }
                        }
                );
    }
}
