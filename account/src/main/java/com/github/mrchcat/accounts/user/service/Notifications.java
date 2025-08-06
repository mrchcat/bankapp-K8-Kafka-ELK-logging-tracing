package com.github.mrchcat.accounts.user.service;

import com.github.mrchcat.accounts.config.ServiceUrl;
import com.github.mrchcat.accounts.security.OAuthHeaderGetter;
import com.github.mrchcat.accounts.user.model.BankUser;
import com.github.mrchcat.shared.notification.BankNotificationDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.security.auth.message.AuthException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Notifications {
    private final String ACCOUNT_SERVICE;

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
        this.ACCOUNT_SERVICE = serviceUrl.getAccount();
    }

    @CircuitBreaker(name = "notifications")
    @Retry(name = "notifications")
    public void sendNotification(BankUser client, String message) throws AuthException {
        var notification = BankNotificationDto.builder()
                .service(ACCOUNT_SERVICE)
                .username(client.getUsername())
                .fullName(client.getFullName())
                .email(client.getEmail())
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
