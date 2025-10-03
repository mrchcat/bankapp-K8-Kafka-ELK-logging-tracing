package com.github.mrchcat.front.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${application.security.maximumSessions:1}")
    private int maximumSessions;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, MeterRegistry meterRegistry) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/login", "/logout/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/registration", "/signup").hasAuthority("MANAGER")
                        .anyRequest().hasAnyAuthority("CLIENT", "MANAGER")
                )
                .oauth2Client(Customizer.withDefaults())
                .formLogin(cst -> cst
                        .successHandler((request, response, authentication) -> {
                            countLogins(true, authentication, meterRegistry);
                            response.sendRedirect("/defaultAfterLogin");
                        })
                        .failureHandler((request, response, authException) -> {
                            countLogins(false, authException.getAuthenticationRequest(), meterRegistry);
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.sendRedirect("/main");
                        })
                )
                .logout(cst -> cst
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(maximumSessions)
                );
        return http.build();
    }

    private void countLogins(Boolean isSuccess, Authentication authentication, MeterRegistry meterRegistry) {
        System.out.println(authentication.getPrincipal().getClass());
        Object principal = authentication.getPrincipal();
        String username = "";
        if (principal instanceof User user) {
            username = user.getUsername();
        } else if (principal instanceof String user) {
            username = user;
        }
        Counter loginCounter = Counter.builder("login")
                .description("Counter of logins with usernames")
                .tag("username", username)
                .tag("is_success", isSuccess.toString())
                .register(meterRegistry);
        loginCounter.increment();
    }

    @Bean
    BCryptPasswordEncoder getEncoder() {
        int strength = 10;
        return new BCryptPasswordEncoder(strength, new SecureRandom());
    }

    @Bean
    AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                                 OAuth2AuthorizedClientService authorizedClientService) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build();
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }
}
