package com.github.mrchcat.accounts.user.service;

import com.github.mrchcat.accounts.config.ServiceUrl;
import com.github.mrchcat.accounts.user.model.BankUser;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import com.github.mrchcat.shared.utils.trace.ToTrace;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class Notifications {
    private final String ACCOUNT_SERVICE;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TracingLogger tracingLogger;
    private final Tracer tracer;

    @Value("${application.kafka.topic.notifications}")
    private String notificationTopic;

    public Notifications(ServiceUrl serviceUrl,
                         KafkaTemplate<String, Object> kafkaTemplate,
                         TracingLogger tracingLogger, Tracer tracer) {
        this.ACCOUNT_SERVICE = serviceUrl.getAccount();
        this.kafkaTemplate = kafkaTemplate;
        this.tracingLogger = tracingLogger;
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
        var currentSpan = tracer.currentSpan();
        kafkaTemplate.send(notificationTopic, notification)
                .whenComplete((result, e) -> {
                            if (e == null) {
                                tracingLogger.info(currentSpan, "В брокер сообщений отправлено уведомление об операции: {}", message);
                            } else {
                                tracingLogger.error(currentSpan, "Ошибка при отправке сообщения в брокер сообщений: {} ", e.getMessage());
                            }
                        }
                );
    }
}
