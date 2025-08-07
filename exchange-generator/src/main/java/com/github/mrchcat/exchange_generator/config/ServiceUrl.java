package com.github.mrchcat.exchange_generator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix="application.service.urls")
public class ServiceUrl {
    private String exchange;
}
