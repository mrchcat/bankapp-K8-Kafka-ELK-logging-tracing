package com.github.mrchcat.front.service;

import com.github.mrchcat.front.config.ServiceUrl;
import com.github.mrchcat.front.security.OAuthHeaderGetter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Monitoring {
    private final AtomicInteger exchangeRatesServiceStatus;
    private final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager;
    private final OAuthHeaderGetter oAuthHeaderGetter;
    private final String EXCHANGE_SERVICE;
    private final String EXCHANGE_GET_ALL_RATES = "/exchange";
    private final RestClient.Builder restClientBuilder;

    public Monitoring(MeterRegistry meterRegistry,
                      OAuthHeaderGetter oAuthHeaderGetter,
                      AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager,
                      ServiceUrl serviceUrl,
                      RestClient.Builder restClientBuilder) {
        this.exchangeRatesServiceStatus = meterRegistry.gauge("exchange_rates_service_status", new AtomicInteger(0));
        this.authorizedClientManager = authorizedClientManager;
        this.EXCHANGE_SERVICE = serviceUrl.getExchange();
        this.restClientBuilder = restClientBuilder;
        this.oAuthHeaderGetter = oAuthHeaderGetter;
    }

    @Scheduled(fixedDelay = 1000L)
    void checkExchangeRatesService() {
        try {
            var oAuthHeader = oAuthHeaderGetter.getOAuthHeader(authorizedClientManager);
            String requestUrl = "http://" + EXCHANGE_SERVICE + EXCHANGE_GET_ALL_RATES;
            var rates = restClientBuilder.build()
                    .get()
                    .uri(requestUrl)
                    .header(oAuthHeader.name(), oAuthHeader.value())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (rates == null) {
                exchangeRatesServiceStatus.set(0);
            } else {
                exchangeRatesServiceStatus.set(1);
            }
        } catch (Exception e) {
            exchangeRatesServiceStatus.set(0);
        }
    }


}
