package com.github.mrchcat.notifications.service;

import com.github.mrchcat.notifications.domain.BankNotification;
import com.github.mrchcat.notifications.repository.NotificationRepository;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import com.github.mrchcat.shared.utils.trace.ToTrace;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MailSender mailSender;
    private final MeterRegistry meterRegistry;
    private final TracingLogger tracingLogger;

    @Value("${application.mail.enabled}")
    private boolean isEmailEnabled;
    @Value("${application.mail.from_address}")
    private String fromMailAddress;
    @Value("${application.mail.sender_name}")
    private String senderName;


    @KafkaListener(topics = {"#{'${application.kafka.topic.notifications}'.split(',')}"})
    @Transactional("transactionManager")
    public void readNotifications(BankNotificationDto dto) {
        var notification = BankNotification.builder()
                .service(dto.service())
                .username(dto.username())
                .fullName(dto.fullName())
                .email(dto.email())
                .message(dto.message())
                .isProcessed(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    @Scheduled(fixedDelay = 1000L)
    public void process() {
        notificationRepository.findAllNotProcessed()
                .forEach(notification -> {
                    if (deliver(notification)) {
                        notificationRepository.setProcessed(notification.getId());
                    }
                });
    }

    @ToTrace(spanName = "deliver")
    private boolean deliver(BankNotification notification) {
        if (!isEmailEnabled) {
            tracingLogger.warn("Уведомление \"{}\" обработано без отправки", notification.getMessage());
            return true;
        }
        try {
            sendByEmail(notification);
            tracingLogger.info("Уведомление \"{}\" отправлено на e-mail:{} ", notification.getMessage(), notification.getEmail());
            return true;
        } catch (Exception e) {
            Counter failedMailsCounter = Counter.builder("notification_delivery_fails")
                    .description("Counter of failed deliveries")
                    .tag("username", notification.getUsername())
                    .register(meterRegistry);
            failedMailsCounter.increment();
            tracingLogger.error("Ошибка отправки уведомления \"{}\" , описание: \"{}\"",notification.getMessage(), e.getMessage());
            return false;
        }
    }

    private void sendByEmail(BankNotification notification) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromMailAddress);
        msg.setTo(notification.getEmail());
        msg.setSubject("notification from: " + senderName);
        msg.setReplyTo(fromMailAddress);
        msg.setText(notification.getMessage());
        mailSender.send(msg);
    }
}
