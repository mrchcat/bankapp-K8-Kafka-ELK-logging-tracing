package com.github.mrchcat.notifications.service;

import com.github.mrchcat.notifications.Repository.NotificationRepository;
import com.github.mrchcat.notifications.domain.BankNotification;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MailSender mailSender;

    @Value("${application.mail.enabled}")
    private boolean isEmailEnabled;
    @Value("${application.mail.from_address}")
    private String fromMailAddress;
    @Value("${application.mail.sender_name}")
    private String senderName;

    private final Tracer tracer;

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

    private boolean deliver(BankNotification notification) {
        var newSpan = tracer.nextSpan().name("deliver").start();
        newSpan.tag("username", notification.getUsername());
        if (!isEmailEnabled) {
            newSpan.tag("delivery_enabled", false);
            newSpan.tag("isSucceed", true);
            log.info("Уведомление \"{}\" обработано без отправки", notification.getMessage());
            return true;
        }
        try {
            newSpan.tag("delivery_enabled", true);
            newSpan.tag("to_mail", notification.getEmail());
            sendByEmail(notification);
            newSpan.tag("isSucceed", true);
            log.info("Уведомление \"{}\" отправлено на e-mail:{} ", notification.getMessage(), notification.getEmail());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            newSpan.tag("isSucceed", false);
            newSpan.tag("error", e.getMessage());
            return false;
        } finally {
            newSpan.end();
        }
    }

    private void sendByEmail(BankNotification notification) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromMailAddress);
        msg.setTo(notification.getEmail());
        msg.setSubject("notification from " + senderName);
        msg.setReplyTo(fromMailAddress);
        msg.setText(notification.getMessage());
        mailSender.send(msg);
    }
}
