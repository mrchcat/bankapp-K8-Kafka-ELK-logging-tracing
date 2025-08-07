package com.github.mrchcat.exchange_generator.service;

import com.github.mrchcat.exchange_generator.config.ServiceUrl;
import com.github.mrchcat.exchange_generator.security.OAuthHeaderGetter;
import com.github.mrchcat.shared.enums.BankCurrency;
import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import com.github.mrchcat.shared.exchange.CurrencyRate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class GeneratorServiceImpl implements GeneratorService {
    private final String EXCHANGE_SERVICE;
    private final String EXCHANGE_SEND_RATES = "/exchange";
    private final BigDecimal AVERAGE_USD = BigDecimal.valueOf(80);
    private final BigDecimal AVERAGE_CNY = BigDecimal.valueOf(11);

    private final RestClient.Builder restClientBuilder;
    private final OAuthHeaderGetter oAuthHeaderGetter;

    public GeneratorServiceImpl(RestClient.Builder restClientBuilder,
                                OAuthHeaderGetter oAuthHeaderGetter,
                                ServiceUrl serviceUrl) {
        this.restClientBuilder = restClientBuilder;
        this.oAuthHeaderGetter = oAuthHeaderGetter;
        this.EXCHANGE_SERVICE=serviceUrl.getExchange();
    }

    @Override
    @Scheduled(fixedDelay = 1000L)
    public void sendNewRates() {
        send(new CurrencyExchangeRatesDto(BankCurrency.RUB, getRates()));
    }

    private List<CurrencyRate> getRates() {
        BigDecimal randomBuyUsd = BigDecimal.valueOf(1 - Math.random() / 10);
        BigDecimal randomSellUsd = BigDecimal.valueOf(1 + Math.random() / 10);

        BigDecimal randomBuyCNY = BigDecimal.valueOf(1 - Math.random() / 10);
        BigDecimal randomSellCNY = BigDecimal.valueOf(1 + Math.random() / 10);

        var USDrate = CurrencyRate.builder()
                .currency(BankCurrency.USD)
                .buyRate(AVERAGE_USD.multiply(randomBuyUsd).setScale(2, RoundingMode.HALF_UP))
                .sellRate(AVERAGE_USD.multiply(randomSellUsd).setScale(2, RoundingMode.HALF_UP))
                .time(LocalDateTime.now())
                .build();
        var CNYrate = CurrencyRate.builder()
                .currency(BankCurrency.CNY)
                .buyRate(AVERAGE_CNY.multiply(randomBuyCNY).setScale(2, RoundingMode.HALF_UP))
                .sellRate(AVERAGE_CNY.multiply(randomSellCNY).setScale(2, RoundingMode.HALF_UP))
                .time(LocalDateTime.now())
                .build();
        return List.of(USDrate, CNYrate);
    }

    private void send(CurrencyExchangeRatesDto rates) {
        try {
            var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
            String url = "http://" + EXCHANGE_SERVICE + EXCHANGE_SEND_RATES;
            restClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(oAuthHeader.name(), oAuthHeader.value())
                    .body(rates)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignore) {
        }
    }

}
