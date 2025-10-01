package com.github.mrchcat.front.service;


import com.github.mrchcat.front.config.ServiceUrl;
import com.github.mrchcat.front.dto.UserDetailsDto;
import com.github.mrchcat.front.mapper.FrontMapper;
import com.github.mrchcat.front.security.OAuthHeaderGetter;
import com.github.mrchcat.shared.accounts.UpdatePasswordRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class CredentialsService implements UserDetailsService, UserDetailsPasswordService {
    private final RestClient.Builder restClientBuilder;
    private final OAuthHeaderGetter oAuthHeaderGetter;
    private final PasswordEncoder encoder;
    private final ServiceUrl serviceUrl;

    private final String ACCOUNT_SERVICE;
    private final String ACCOUNTS_GET_USER_DETAILS_API = "/credentials";
    private final String ACCOUNTS_UPDATE_USER_PASSWORD_API = "/credentials";

    public CredentialsService(RestClient.Builder restClientBuilder,
                              OAuthHeaderGetter oAuthHeaderGetter,
                              PasswordEncoder encoder,
                              ServiceUrl serviceUrl,
                              MeterRegistry meterRegistry) {
        this.restClientBuilder = restClientBuilder;
        this.oAuthHeaderGetter = oAuthHeaderGetter;
        this.encoder = encoder;
        this.ACCOUNT_SERVICE = serviceUrl.getAccount();
        this.serviceUrl = serviceUrl;
    }

    @Override
    @CircuitBreaker(name = "accounts")
    @Retry(name = "accounts")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
            var response = restClientBuilder.build()
                    .get()
                    .uri("http://" + ACCOUNT_SERVICE + ACCOUNTS_GET_USER_DETAILS_API + "/" + username)
                    .header(oAuthHeader.name(), oAuthHeader.value())
                    .retrieve()
                    .body(UserDetailsDto.class);
            if (response == null) {
                throw new UsernameNotFoundException("сервис accounts вернул пустой ответ");
            }
            return FrontMapper.toUserDetails(response);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new UsernameNotFoundException(username + "not found");
            }
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @CircuitBreaker(name = "accounts")
    @Retry(name = "accounts")
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        String passwordHash = encoder.encode(newPassword);
        try {
            var oAuthHeader = oAuthHeaderGetter.getOAuthHeader();
            var response = restClientBuilder.build()
                    .post()
                    .uri("http://" + ACCOUNT_SERVICE + ACCOUNTS_UPDATE_USER_PASSWORD_API + "/" + user.getUsername())
                    .header(oAuthHeader.name(), oAuthHeader.value())
                    .body(new UpdatePasswordRequestDto(passwordHash))
                    .retrieve()
                    .body(UserDetailsDto.class);
            if (response == null) {
                throw new UsernameNotFoundException("сервис accounts вернул пустой ответ");
            }
            return FrontMapper.toUserDetails(response);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new UsernameNotFoundException(user + "not found");
            }
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
