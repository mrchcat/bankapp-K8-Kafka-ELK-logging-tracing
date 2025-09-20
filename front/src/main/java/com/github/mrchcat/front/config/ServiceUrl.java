package com.github.mrchcat.front.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.service.urls")
public class ServiceUrl {
    private String front;
    private String account;
    private String cash;
    private String transfer;
    private String exchange;
}
