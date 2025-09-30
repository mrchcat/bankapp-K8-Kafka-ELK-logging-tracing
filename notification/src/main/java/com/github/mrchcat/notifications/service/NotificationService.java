package com.github.mrchcat.notifications.service;

import com.github.mrchcat.notifications.Repository.NotificationRepository;
import com.github.mrchcat.notifications.domain.BankNotification;
import com.github.mrchcat.shared.notification.BankNotificationDto;
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

    @Value("${application.mail.from_address}")
    private String fromMailAddress;
    @Value("${application.mail.sender}")
    private String senderName;
    @Value("${application.mail.enabled}")
    private boolean isEmailEnabled;


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
                    if (!isEmailEnabled || sendByEmail(notification)) {
                        notificationRepository.setProcessed(notification.getId());
                    }
                });
    }

    private boolean sendByEmail(BankNotification notification) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromMailAddress);
            msg.setTo(notification.getEmail());
            msg.setText(notification.getMessage());
            mailSender.send(msg);
            log.info("Уведомление \"{}\" отправлено на почту {}", notification.getMessage(), notification.getEmail());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
