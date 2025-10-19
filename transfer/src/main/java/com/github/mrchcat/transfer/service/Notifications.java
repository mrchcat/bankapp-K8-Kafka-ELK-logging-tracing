package com.github.mrchcat.transfer.service;

import com.github.mrchcat.shared.accounts.BankUserDto;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import com.github.mrchcat.shared.utils.trace.ToTrace;
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
    private final TracingLogger tracingLogger;

    @Value("${application.kafka.topic.notifications}")
    private String notificationTopic;

    public Notifications(KafkaTemplate<String, Object> kafkaTemplate,
                         ServiceUrl serviceUrl,
                         Tracer tracer,
                         TracingLogger tracingLogger) {
        this.TRANSFER_SERVICE = serviceUrl.getTransfer();
        this.kafkaTemplate = kafkaTemplate;
        this.tracer = tracer;
        this.tracingLogger = tracingLogger;
    }

    @ToTrace(spanName = "kafka", tags = {"operation:send_notification"})
    public void sendNotification(BankUserDto client, String message) {
        var notification = BankNotificationDto.builder()
                .service(TRANSFER_SERVICE)
                .username(client.username())
                .fullName(client.fullName())
                .email(client.email())
                .message(message)
                .build();
        var currentSpan = tracer.currentSpan();
        kafkaTemplate.send(notificationTopic, notification).whenComplete((result, e) -> {
                    if (e == null) {
                        tracingLogger.info(currentSpan, "В брокер сообщений отправлено уведомление об операции: {}", message);
                    } else {
                        tracingLogger.error(currentSpan, "Ошибка при отправке сообщения в брокер сообщений: {} ", e.getMessage());
                    }
                }
        );
    }
}
