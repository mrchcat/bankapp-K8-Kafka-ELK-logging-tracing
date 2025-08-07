package com.github.mrchcat.transfer.service;

import com.github.mrchcat.shared.accounts.BankUserDto;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import com.github.mrchcat.transfer.config.ServiceUrl;
import com.github.mrchcat.transfer.security.OAuthHeaderGetter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.security.auth.message.AuthException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Notifications {
    private final String TRANSFER_SERVICE;

    private final String NOTIFICATION_SERVICE;
    private final String NOTIFICATION_SEND_NOTIFICATION = "/notification";

    private final RestClient.Builder restClientBuilder;
    private final OAuthHeaderGetter oAuthHeaderGetter;


    public Notifications(RestClient.Builder restClientBuilder,
                         OAuthHeaderGetter oAuthHeaderGetter,
                         ServiceUrl serviceUrl) {
        this.restClientBuilder = restClientBuilder;
        this.oAuthHeaderGetter = oAuthHeaderGetter;
        this.NOTIFICATION_SERVICE = serviceUrl.getNotifications();
        this.TRANSFER_SERVICE = serviceUrl.getTransfer();
    }

    @CircuitBreaker(name = "notifications")
    @Retry(name = "notifications")
    public void sendNotification(BankUserDto client, String message) throws AuthException {
        var notification = BankNotificationDto.builder()
                .service(TRANSFER_SERVICE)
                .username(client.username())
                .fullName(client.fullName())
                .email(client.email())
                .message(message)
                .build();
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        String requestUrl = "http://" + NOTIFICATION_SERVICE + NOTIFICATION_SEND_NOTIFICATION;
        restClientBuilder.build()
                .post()
                .uri(requestUrl)
                .header(oAuthHeader.name(), oAuthHeader.value())
                .body(notification)
                .retrieve()
                .toBodilessEntity();
    }
}
