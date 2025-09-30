package com.github.mrchcat.notifications.service;

import com.github.mrchcat.notifications.Repository.NotificationRepository;
import com.github.mrchcat.notifications.domain.BankNotification;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

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
                    sendByEmail(notification);
                    notificationRepository.setProcessed(notification.getId());
                });
    }

    private void sendByEmail(BankNotification notification) {
        String message = String.format("Уведомление \"%s\" отправлено по почте", notification);
        log.info(message);
    }

}
