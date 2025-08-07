package com.github.mrchcat.transfer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "application.service.urls")
public class ServiceUrl {
    private String account;
    private String exchange;
    private String blocker;
    private String notifications;
    private String transfer;
}