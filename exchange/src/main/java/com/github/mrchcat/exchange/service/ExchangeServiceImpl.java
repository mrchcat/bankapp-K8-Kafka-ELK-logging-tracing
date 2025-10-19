package com.github.mrchcat.exchange.service;

import com.github.mrchcat.exchange.exceptions.ExchangeGeneratorServiceException;
import com.github.mrchcat.exchange.model.CurrencyExchangeRecord;
import com.github.mrchcat.exchange.repository.ExchangeRepository;
import com.github.mrchcat.shared.enums.BankCurrency;
import com.github.mrchcat.shared.exchange.CurrencyExchangeRateDto;
import com.github.mrchcat.shared.exchange.CurrencyExchangeRatesDto;
import com.github.mrchcat.shared.exchange.CurrencyRate;
import com.github.mrchcat.shared.utils.log.TracingLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {
    private final ExchangeRepository exchangeRepository;
    private final TracingLogger tracingLogger;


    private static final ConcurrentHashMap<BankCurrency, CurrencyRate> exchangeRates = new ConcurrentHashMap<>();
    private static final BankCurrency baseCurrencyByDefault = BankCurrency.RUB;

    static {
        exchangeRates.put(baseCurrencyByDefault,
                new CurrencyRate(baseCurrencyByDefault, BigDecimal.ONE, BigDecimal.ONE, null));
    }

    @Override
    public CurrencyExchangeRateDto getExchangeRate(BankCurrency fromCurrency, BankCurrency toCurrency) {
        var rateDto = CurrencyExchangeRateDto.builder()
                .from(fromCurrency)
                .to(toCurrency)
                .build();
        //если валюты совпадают
        if (fromCurrency.equals(toCurrency)) {
            rateDto.setRate(BigDecimal.ONE);
            return rateDto;
        }
        //если меняем на основную валюту
        if (!exchangeRates.containsKey(fromCurrency)) {
            throw new NoSuchElementException(fromCurrency.name());
        }
        if (toCurrency.equals(baseCurrencyByDefault)) {
            rateDto.setRate(exchangeRates.get(fromCurrency).sellRate());
            return rateDto;
        }
//        если обе валюты не основные
        if (!exchangeRates.containsKey(toCurrency)) {
            throw new NoSuchElementException(toCurrency.name());
        }
        BigDecimal fromCurrencyInDefault = exchangeRates.get(fromCurrency).sellRate();
        BigDecimal toCurrencyInDefault = exchangeRates.get(toCurrency).buyRate();
        BigDecimal rate = fromCurrencyInDefault.divide(toCurrencyInDefault, 5, RoundingMode.CEILING);
        rateDto.setRate(rate);
        tracingLogger.debug("Отправлены курсы валют {}", rateDto);
        return rateDto;
    }

    @Override
    public Collection<CurrencyRate> getAllRates() {
        var rates = exchangeRates.values();
        tracingLogger.debug("Отправлены курсы валют {}", rates);
        return rates;
    }

    @KafkaListener(topics = {"#{'${application.kafka.topic.rates}'.split(',')}"})
    public void saveRates(CurrencyExchangeRatesDto rates) {
        if (!rates.baseCurrency().equals(baseCurrencyByDefault)) {
            throw new ExchangeGeneratorServiceException("");
        }
        for (CurrencyRate currencyRate : rates.exchangeRates()) {
            BankCurrency currency = currencyRate.currency();
            if (!currency.equals(baseCurrencyByDefault)) {
                exchangeRates.put(currency, currencyRate);
                var record = CurrencyExchangeRecord.builder()
                        .baseCurrency(baseCurrencyByDefault)
                        .exchangeCurrency(currency)
                        .buyRate(currencyRate.buyRate())
                        .sellRate(currencyRate.sellRate())
                        .time(currencyRate.time())
                        .build();
                exchangeRepository.save(record);
            }
        }
    }
}
