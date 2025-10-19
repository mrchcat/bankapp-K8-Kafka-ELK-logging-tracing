package com.github.mrchcat.front.service;

import com.github.mrchcat.front.config.ServiceUrl;
import com.github.mrchcat.front.dto.FrontBankUserDto;
import com.github.mrchcat.front.dto.FrontCashTransactionDto;
import com.github.mrchcat.front.dto.FrontEditUserAccountDto;
import com.github.mrchcat.front.dto.FrontRate;
import com.github.mrchcat.front.dto.NewClientRegisterDto;
import com.github.mrchcat.front.dto.NonCashTransfer;
import com.github.mrchcat.front.dto.UserDetailsDto;
import com.github.mrchcat.front.exception.ExchangeServiceException;
import com.github.mrchcat.front.mapper.FrontMapper;
import com.github.mrchcat.front.model.FrontCurrencies;
import com.github.mrchcat.front.security.OAuthHeaderGetter;
import com.github.mrchcat.shared.accounts.BankUserDto;
import com.github.mrchcat.shared.cash.CashTransactionDto;
import com.github.mrchcat.shared.enums.BankCurrency;
import com.github.mrchcat.shared.enums.CashAction;
import com.github.mrchcat.shared.exchange.CurrencyRate;
import com.github.mrchcat.shared.transfer.NonCashTransferDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.security.auth.message.AuthException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.naming.ServiceUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FrontServiceImpl implements FrontService {
    private final UserDetailsPasswordService userDetailsPasswordService;
    private final UserDetailsService userDetailsService;
    private final RestClient.Builder restClientBuilder;
    private final OAuthHeaderGetter oAuthHeaderGetter;
    private final PasswordEncoder encoder;
    private final ServiceUrl serviceUrl;


    private final String ACCOUNT_SERVICE;
    private final String ACCOUNTS_REGISTER_NEW_CLIENT_API = "/registration";
    private final String ACCOUNTS_GET_CLIENT_API = "/account";
    private final String ACCOUNTS_PATCH_CLIENT_API = "/account";

    private final String CASH_SERVICE;
    private final String CASH_PROCESS_API = "/cash";

    private final String TRANSFER_SERVICE;
    private final String TRANSFER_PROCESS_API = "/transfer";

    private final String EXCHANGE_SERVICE;
    private final String EXCHANGE_GET_ALL_RATES = "/exchange";

    public FrontServiceImpl(UserDetailsPasswordService userDetailsPasswordService,
                            UserDetailsService userDetailsService,
                            RestClient.Builder restClientBuilder,
                            OAuthHeaderGetter oAuthHeaderGetter,
                            PasswordEncoder encoder,
                            ServiceUrl serviceUrl) {
        this.userDetailsPasswordService = userDetailsPasswordService;
        this.userDetailsService = userDetailsService;
        this.restClientBuilder = restClientBuilder;
        this.oAuthHeaderGetter = oAuthHeaderGetter;
        this.encoder = encoder;
        this.serviceUrl = serviceUrl;
        this.ACCOUNT_SERVICE = serviceUrl.getAccount();
        this.CASH_SERVICE = serviceUrl.getCash();
        this.TRANSFER_SERVICE = serviceUrl.getTransfer();
        this.EXCHANGE_SERVICE = serviceUrl.getExchange();
    }

    @Override
    @CircuitBreaker(name = "accounts")
    @Retry(name = "accounts")
    public UserDetails editClientPassword(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return userDetailsPasswordService.updatePassword(userDetails, password);
    }

    @Override
    @CircuitBreaker(name = "accounts")
    @Retry(name = "accounts")
    public UserDetails registerNewClient(NewClientRegisterDto ncrdto) throws AuthException {
        String passwordHash = encoder.encode(ncrdto.password());
        var newClientRequestDto = FrontMapper.toCreateNewClientRequestDto(ncrdto, passwordHash);
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        var response = restClientBuilder.build()
                .post()
                .uri("http://" + ACCOUNT_SERVICE + ACCOUNTS_REGISTER_NEW_CLIENT_API)
                .header(oAuthHeader.name(), oAuthHeader.value())
                .body(newClientRequestDto)
                .retrieve()
                .body(UserDetailsDto.class);
        if (response == null) {
            throw new UsernameNotFoundException("сервис accounts вернул пустой ответ");
        }
        return FrontMapper.toUserDetails(response);
    }

    @Override
    @CircuitBreaker(name = "accounts", fallbackMethod = "fallBackFrontBankUserDto")
    @Retry(name = "accounts", fallbackMethod = "fallBackFrontBankUserDto")
    public FrontBankUserDto getClientDetailsAndAccounts(String username) throws AuthException {
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        BankUserDto bankUserDto = restClientBuilder.build()
                .get()
                .uri("http://" + ACCOUNT_SERVICE + ACCOUNTS_GET_CLIENT_API + "/" + username)
                .header(oAuthHeader.name(), oAuthHeader.value())
                .retrieve()
                .body(BankUserDto.class);
        if (bankUserDto == null) {
            throw new UsernameNotFoundException("сервис accounts вернул пустой ответ");
        }
        return FrontMapper.toFrontBankUserDto(bankUserDto);
    }

    private FrontBankUserDto fallBackFrontBankUserDto(Throwable t) {
        return FrontBankUserDto.builder()
                .username("error: service not available")
                .email("error: service not available")
                .birthDay(LocalDate.of(1900, 1, 1))
                .fullName("error: service not available")
                .accounts(Collections.emptyList())
                .build();
    }

    @Override
    @CircuitBreaker(name = "accounts", fallbackMethod = "fallBackBankUserDto")
    @Retry(name = "accounts", fallbackMethod = "fallBackBankUserDto")
    public BankUserDto editUserAccount(String username, FrontEditUserAccountDto frontEditUserAccountDto) throws AuthException {
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        var response = restClientBuilder.build()
                .patch()
                .uri("http://" + ACCOUNT_SERVICE + ACCOUNTS_PATCH_CLIENT_API + "/" + username)
                .header(oAuthHeader.name(), oAuthHeader.value())
                .body(FrontMapper.toRequestDto(frontEditUserAccountDto))
                .retrieve()
                .body(BankUserDto.class);
        if (response == null) {
            throw new UsernameNotFoundException("сервис accounts вернул пустой ответ");
        }
        return response;
    }

    private BankUserDto fallBackBankUserDto(Throwable t) {
        return BankUserDto.builder()
                .id(UUID.randomUUID())
                .username("error: service not available")
                .fullName("error: service not available")
                .birthDay(LocalDate.of(1900, 1, 1))
                .accounts(Collections.emptyList())
                .build();
    }

    @Override
    @CircuitBreaker(name = "accounts", fallbackMethod = "fallBackList")
    @Retry(name = "accounts", fallbackMethod = "fallBackList")
    public List<FrontBankUserDto> getAllClientsWithActiveAccounts() throws AuthException, ServiceUnavailableException {
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        List<BankUserDto> response = restClientBuilder.build()
                .get()
                .uri("http://" + ACCOUNT_SERVICE + ACCOUNTS_GET_CLIENT_API)
                .header(oAuthHeader.name(), oAuthHeader.value())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (response == null) {
            throw new ServiceUnavailableException("сервис аккаунтов не доступен");
        }
        return FrontMapper.toFrontBankUserDto(response);
    }

    private List<FrontBankUserDto> fallBackList(Throwable t) {
        return Collections.emptyList();
    }

    @Override
    @CircuitBreaker(name = "cash")
//    @Retry(name = "cash")
    public void processCashOperation(String username, FrontCashTransactionDto cashOperationDto, CashAction action) throws AuthException {
        CashTransactionDto requestDto = FrontMapper.toRequestDto(username, cashOperationDto, action);
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        restClientBuilder.build()
                .post()
                .uri("http://" + CASH_SERVICE + CASH_PROCESS_API)
                .header(oAuthHeader.name(), oAuthHeader.value())
                .body(requestDto)
                .retrieve()
                .body(String.class);
    }

    @Override
    @CircuitBreaker(name = "transfer")
//    @Retry(name = "transfer")
    public void processNonCashOperation(NonCashTransfer nonCashTransfer) throws AuthException {
        NonCashTransferDto requestDto = FrontMapper.toRequestDto(nonCashTransfer);
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        restClientBuilder.build()
                .post()
                .uri("http://" + TRANSFER_SERVICE + TRANSFER_PROCESS_API)
                .header(oAuthHeader.name(), oAuthHeader.value())
                .body(requestDto)
                .retrieve()
                .body(String.class);
    }

    @Override
    @CircuitBreaker(name = "exchange")
    @Retry(name = "exchange", fallbackMethod = "fallbackExchange")
    public List<FrontRate> getAllRates() throws AuthException {
        Collection<CurrencyRate> rateList = getAllRatesFromExchange();
        Map<BankCurrency, CurrencyRate> rateMap = new HashMap<>();
        rateList.forEach(cr -> rateMap.put(cr.currency(), cr));
        List<FrontRate> frontRates = new ArrayList<>();
        for (FrontCurrencies.BankFrontCurrency frontCurrency : FrontCurrencies.getCurrencyList()) {
            if (frontCurrency.name().equals("RUB")) {
                continue;
            }
            BankCurrency currency = BankCurrency.valueOf(frontCurrency.name());
            if (rateMap.containsKey(currency)) {
                CurrencyRate currencyRate = rateMap.get(currency);
                var frontRate = FrontRate.builder()
                        .currencyCode(frontCurrency.name())
                        .title(frontCurrency.title)
                        .buyRate(currencyRate.buyRate())
                        .sellRate(currencyRate.sellRate())
                        .build();
                frontRates.add(frontRate);
            }
        }
        return frontRates;
    }

    private List<FrontRate> fallbackExchange(Throwable t) {
        return List.of(new FrontRate("error",
                "service unavailable",
                BigDecimal.ONE.negate(),
                BigDecimal.ONE.negate()));
    }

    private Collection<CurrencyRate> getAllRatesFromExchange() throws AuthException {
        var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
        String requestUrl = "http://" + EXCHANGE_SERVICE + EXCHANGE_GET_ALL_RATES;
        try {
            Collection<CurrencyRate> rates = restClientBuilder.build()
                    .get()
                    .uri(requestUrl)
                    .header(oAuthHeader.name(), oAuthHeader.value())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (rates == null) {
                throw new ExchangeServiceException("");
            }
            return rates;
        } catch (Exception ex) {
            throw new ExchangeServiceException("");
        }
    }
}
